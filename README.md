![image]([https://raw.githubusercontent.com/TRLVMMR/distributed_transaction/master/%24GDMI%408%7B0WKFH0J0ZNLRE73.png](https://raw.githubusercontent.com/TRLVMMR/distributed_transaction/master/123.png)

这叫独立消息的最终一致性，有三个角色

1. 生产者
2. 消费者
3. 消息处理者：定时任务进行补偿



但本人将上图消息处理者的的消息确认使用一个本地的业务标记，用来标志着一个本地事务的完成。之后分为两个阶段，一个是调用confirm确认是否存在（目的是为了确认生产者是否已经完成了这个业务，还是已经回滚），然后再调用deleteBusiness删除业务标记，此两个接口由生产者提供，消息处理者远程调用。

因为若不分为两步，改为一步，按上图的方式：执行顺序为：	执行RPC→修改消息为可发送。但是消息处理者可能还没修改消息的状态，就宕机了，而生产者已经删除了业务标记，那么这个消息没有存到redis，更不会发到mq，消费者自然也就不可能消费到。因此可能需要提供两个接口，一个确认，一个删除。执行顺序是：消息确认→消息状态改变→删除业务号。这样，就算消息处理者宕机了，之后重新上线，去向生产者重新确认，业务标记也是还在的。而若果宕机导致业务号忘记删除也无所谓，因为消息已经改成可发送了，不会重复确认，唯一的缺点是如果宕机，可能导致生产者那边忘记删除，从而产生内存泄漏，解决方法是给缓存加个失效时间或者定时删除。

```java
@Component
@FeignClient(value = "transaction-order")
public interface ProviderService {
    @PostMapping("/rpc/confirm")
    public boolean confirm(@RequestBody String msgId);

    @PostMapping("/rpc/deleteBusiness")
    public String deleteBusiness(@RequestBody String msgId);
}
```

防止消息处理者还没把消息存储到redis，自身就宕机的情况。另外，提供submit跟rollback接口。给生产者成功和失败时调用。

```java
@Service
@FeignClient(value = "transaction-msghandler")
public interface MsgHandlerService {

    @PostMapping("/rpc/sendHalfMessage")
    public String sendHalfMessage(@RequestBody MQMessage msg);

    @Async
    @PostMapping("/rpc/submit")
    public String submit(@RequestBody MQMessage msg);

    @Async
    @PostMapping("/rpc/rollback")
    public String rollback(@RequestBody MQMessage msg);
}
```



```
消息：
1. 状态1，待发送状态
2. 状态2，可发送状态
3. 状态3，完成状态（直接删除可发送状态的消息）

生产者：
1. 不用配置rabbitmq，而是使用RPC调用，让配置有rabbitmq的消息处理者代发。自己只负责生产消息 
2. （暴露接口）需要提供消息确认的接口。目前想到的实现办法是，事务最后需要设置redis或者本地缓存，比如存储业务号, 一旦确认成功，就由消息处理者回调并删除，如果缓存中没有这个业务号，就证明还没执行完或者已经回滚，重试几次后就放弃。
3.（暴露接口）可能还需要提供业务号的删除接口： 按2的方法执行方法，执行顺序：	执行RPC→修改消息为可发送。但是消息处理者可能还没修改消息的状态，就宕机了。因此可能需要提供两个接口，一个确认，一个删除。执行顺序是：消息确认→消息状态改变→删除业务号。这样，就算宕机导致业务号忘记删除也无所谓，因为消息已经改成可发送了，不会重复确认，而生产者那边设置的业务号到时也可以设置过期时间。
4. 业务执行结束时通知消息处理者，异步调用确认方法，形成回调。

消息处理者：
1. 基本方法：设置为待发送，可发送，完成状态的。
2. （暴露接口）向外暴露设置待发送方法。
3. 定时遍历待发送状态的消息，RPC调用生产者的确认方法进行确认，一旦到达时间上限，就失败，删除待发送消息。若成功，修改消息状态为可发送状态
4. 定时遍历可发送状态的消息，将可发送的消息发送给RabbitMQ，一旦消息ack确认成，删除可发送状态的消息，代表已经完成。如果ack失败，继续发送，一旦达到次数上限，抛异常，进入死信队列。
5. （暴露接口）3的子方法sendHalfMessage，只处理单一消息。非定时，确认并修改状态。提供此子方法的原因是方便其他人调用
6. （暴露接口）4的子方法submit，只处理单一消息，非定时，发送消息给rabbitmq。
7. （暴露接口）rollback，删除redis内的待发送消息。


消费者：
1. 进行消息幂等性检查，这里可以用数据库的唯一索引来检查幂等性就行了
2. 手动ack，在业务结束时进行

```



完整调用流程：

1. 生产者远程调用sendHalfMessage
2. 消息处理者收到消息，在redis中加入此消息，key为订单号，并设置为待发送状态
3. 生产者开始执行业务
4. 生产者执行业务结束，设置内存标记
5. （可异步）生产者远程调用调用submit方法。
6. 消息处理者收到消息，远程调用生产者的confirm方法
7. 生产者收到消息，查看缓存，确认业务是否完成，如果业务完成，则返回true。
8. 若返回true，消息处理者就在将redis中此订单号的消息设为可发送状态。
9. 然后消息处理者再远程调用生产者的deleteBusiness方法，通知生产者在缓存中删除此业务号。
10. 最后，消息处理者将此消息发送消息到MQ。
11. 若消息成功发送到MQ，回调ACK机制，消息处理者检查ack是否为true，若是，删除此订单号在redis中的消息
12. （可异步）若出现异常，生成者调用rollback方法，消息处理者收到消息，删除