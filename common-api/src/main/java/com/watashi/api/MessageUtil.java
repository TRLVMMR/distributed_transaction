package com.watashi.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageUtil {
    // status的值，如果为-1代表是Prepare状态， 如果大于0代表是Ready状态，且为已重试发送次数
    public static final int Ready = 0;
    public static final int Prepare = -1;
    // 设置最大超时时间，默认1分钟。这个超时时间既是重新确认的时间上限，也是重新发到mq的时间上限。有需求可以分开
    public static final long MAX_DISTANCE = 1 * 1000 * 60;

    /**
     * 人如其名，过期时返回true
     * @return
     */
     public static boolean isTimeout(MQMessage mqMessage){
        Long timestamp = mqMessage.getTimestamp();
        long deleteTime = timestamp + MAX_DISTANCE;
        long now = new Date().getTime();
        return now > deleteTime;
    }

    /**
     * 返回重试发送给RabbitMQ的次数，若消息是prepare状态，则返回0。注意是重试次数，不是发送次数
     * @return
     */

    public static int getResendVal(MQMessage mqMessage){
        Integer status = mqMessage.getStatus();
        return status > 0 ? status : 0;
    }

    /**
     * 返回确认消息的次数，若消息是Ready状态，返回0，需要注意的是生产者会调用了一次，所以为-1。
     * @return
     */
    public static int getConfirmVal(MQMessage mqMessage){
        Integer status = mqMessage.getStatus();
        return status < 0 ? -status : 0;
    }

    public static int incrResendVal(MQMessage mqMessage){
        Integer status = mqMessage.getStatus();
        if(status > 0)
            status += 1;
        return status;
    }

    public static int incrConfirmVal(MQMessage mqMessage){
        Integer status = mqMessage.getStatus();
        if(status < 0)
            status += 1;
        return status;
    }
    public static boolean isReady(MQMessage mqMessage){
        Integer status = mqMessage.getStatus();
        return status >= Ready;
    }

    public static boolean isPrepare(MQMessage mqMessage){
        Integer status = mqMessage.getStatus();
        return status == Prepare;
    }
}
