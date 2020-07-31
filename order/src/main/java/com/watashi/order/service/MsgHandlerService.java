package com.watashi.order.service;

import com.watashi.api.MQMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
