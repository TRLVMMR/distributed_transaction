package com.watashi.msghandler.mqwork;


import com.watashi.api.MQConstants;
import com.watashi.api.MessageUtil;
import com.watashi.api.MQMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
@Slf4j
public class RedisCoordinator implements Coordinator {
    // 由于stream是懒加载的，如果写成局部变量会导致在方法执行时，超时队列的size一直为0，
    private static List<String> timeoutQueue = new ArrayList<>();

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void addMsgPrepare(MQMessage msg) {
        log.info("保存msg" + msg.getMsgId());
        redisTemplate.opsForHash().put(MQConstants.BUSINESS_KEY, msg.getMsgId(), msg);
    }

    @Override
    public void deleteMsgPrepare(MQMessage msg) {
        // 没有检查是否为prepare状态
        redisTemplate.opsForHash().delete(MQConstants.BUSINESS_KEY,msg.getMsgId());
    }

    /**
     *
     * @param msgId
     */
    @Override
    public void setMsgReady(String msgId) throws IOException {
        Object o = redisTemplate.opsForHash().get(MQConstants.BUSINESS_KEY, msgId);
        if(o == null)
            throw new IOException("此消息不存在" + msgId);
        MQMessage mqMessage = (MQMessage) o;
        mqMessage.setStatus(MessageUtil.Ready);
        redisTemplate.opsForHash().put(MQConstants.BUSINESS_KEY, mqMessage.getMsgId(), mqMessage);
    }

    @Override
    public void setMsgSuccess(String msgId) {
        // 成功时删掉redis存储的key
        redisTemplate.opsForHash().delete(MQConstants.BUSINESS_KEY, msgId);
    }

    @Override
    public MQMessage getMetaMsg(String msgId) {
        return (MQMessage) redisTemplate.opsForHash().get(MQConstants.BUSINESS_KEY, msgId);
    }

    @Override
    public Stream<MQMessage> getMsgPrepare(){
        Stream<MQMessage> mqMessages = filterTimeout();
        return mqMessages.filter(MessageUtil::isPrepare);
    }

    @Override
    public Stream<MQMessage> getMsgReady(){
        Stream<MQMessage> mqMessages = filterTimeout();
        return mqMessages.filter(MessageUtil::isReady);
    }


    private Stream<MQMessage> filterTimeout(){
        HashOperations hashOperations = redisTemplate.opsForHash();

        List<MQMessage> mqMessageList = hashOperations.values(MQConstants.BUSINESS_KEY);
        // 超时队列

        // 遍历所有超时的prepare消息
        Stream<MQMessage> mqMessageStream = mqMessageList.stream()
                .filter(item -> {
                    // 需要注意的是Stream是懒加载，整个stream执行的时候才会执行filter
                    if (MessageUtil.isTimeout(item)) {
                        timeoutQueue.add(item.getMsgId());
                        return false;
                    }
                    return true;
                });

        // 删除超时队列的内容。后面觉得就算用Pipeline，forEach一个个删还是太沙雕了，因此注释掉
//        redisTemplate.executePipelined(new RedisCallback<Object>() {
//            @Override
//            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
//                timeoutQueue.forEach((item) ->{
//                    redisConnection.hashCommands()
//                            .hDel(MQConstants.BUSINESS_KEY.getBytes(), item.getBytes());
//                });
//                return null;
//            }
//        });

        // 需要注意，由于本人的锅，忘记Stream是执行时才把前面的filter一起调用，这里是先判断是否为空，
        // 然后才调用Stream的foreach方法时才执行上面的filter内容，因此这里是删掉前一次调度的超时队列
        if (timeoutQueue.isEmpty())
            return mqMessageStream;
        hashOperations.delete(MQConstants.BUSINESS_KEY, timeoutQueue.toArray());
        // 将超时队列清空
        timeoutQueue.clear();
        return mqMessageStream;
    }
}
