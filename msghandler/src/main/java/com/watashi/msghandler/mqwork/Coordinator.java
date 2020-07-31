package com.watashi.msghandler.mqwork;

import com.watashi.api.MQMessage;

import java.io.IOException;
import java.util.stream.Stream;

public interface Coordinator {

    /**设置消息为prepare状态*/
    void addMsgPrepare(MQMessage msg);

    void deleteMsgPrepare(MQMessage msg);

    /**设置消息为ready状态，删除prepare状态*/
    void setMsgReady(String msgId) throws IOException;

    /**消息发送成功，删除ready状态消息*/
    void setMsgSuccess(String msgId);

    /**从db中获取消息实体*/
    MQMessage getMetaMsg(String msgId);

    /**获取ready状态消息*/
    Stream<MQMessage> getMsgReady();

    /**获取prepare状态消息*/
    Stream<MQMessage> getMsgPrepare();

}