package com.watashi.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Accessors(chain = true)
@Setter
@Getter
@ToString
public class MQMessage implements Serializable {

    /**MQ的消息状态
     1. prepare：代表协调者已经将message存入硬盘
     2. ready： 代表生产者已经执行完自己的事务
     3. success： 代表mq已经将消息发出去
     **/
    private String msgId;
    // 默认消息创建出来为待发送状态
    private Integer status = MessageUtil.Prepare;
    private Long timestamp = new Date().getTime();
    private Object body;

}
