package com.watashi.msghandler.contoller;


import com.watashi.api.MQMessage;
import com.watashi.msghandler.mqwork.TimeCompensation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RPCController {

    @Autowired
    TimeCompensation timeCompensation;



    @PostMapping("/rpc/sendHalfMessage")
    public String sendHalfMessage(@RequestBody MQMessage msg){
        log.info("接收到提供者的半消息，开始保存半消息" + msg.getMsgId());
        timeCompensation.setHalfMessage(msg);
        return "success";
    }

    @PostMapping("/rpc/submit")
    public String submit(@RequestBody MQMessage msg){
        log.info("接收到提供者的请求，准备submit" + msg.getMsgId());
        timeCompensation.confirm(msg);
        return "success";
    }

    @PostMapping("/rpc/rollback")
    public String rollback(@RequestBody MQMessage msg){
        log.info("接收到提供者的请求，准备rollback" + msg.getMsgId());
        timeCompensation.rollback(msg);
        return "success";
    }
}
