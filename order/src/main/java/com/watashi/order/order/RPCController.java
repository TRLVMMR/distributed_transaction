package com.watashi.order.order;

import com.watashi.order.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RPCController {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/rpc/confirm")
    public boolean confirm(@RequestBody String msgId){
        // 确认行为
        log.info("我是提供者，正在确认业务是否已经完成");
        return transactionService.existBusiness(msgId);
    }

    @PostMapping("/rpc/deleteBusiness")
    public String deleteBusiness(@RequestBody String msgId){
        log.info("我是提供者，正在删除业务标记");
        transactionService.deleteBusiness(msgId);
        return "success";
    }

}
