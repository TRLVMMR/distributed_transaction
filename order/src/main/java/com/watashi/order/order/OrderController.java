package com.watashi.order.order;

import com.watashi.api.MQMessage;
import com.watashi.api.Order;
import com.watashi.order.service.MsgHandlerService;
import com.watashi.order.service.OrderService;
import com.watashi.order.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;

@RestController
@Slf4j
public class OrderController {

    public static final String HOST = "http://transaction-pay";

    @Autowired
    TransactionService transactionService;

    @Autowired
    OrderService orderService;

    @Autowired
    MsgHandlerService msgHandlerService;

    @GetMapping("/order/create")
    public String createOrder(@RequestParam(required = false) Integer id, @RequestParam(required = false) String content){
        long orderId;
        // 模拟生成订单，实际上可以用雪花算法之类的
        if(id != null)
            orderId = id;
        else
            orderId = new Random().nextInt( 999999) + 99999;
        String orderContent;
        if(content != null)
            orderContent = content;
        else
            orderContent  = "这是订单内容";
        Order order = new Order().setOrderId(orderId).setContent(orderContent);

        MQMessage mqMessage = new MQMessage().setMsgId(String.valueOf(orderId)).setBody(orderContent);

        // 发送半消息，开始业务
        msgHandlerService.sendHalfMessage(mqMessage);
        try {
            log.info("开始业务" + orderId);
            orderService.save(order);
            log.info("结束业务" + orderId);
            // 本地放入标记
            transactionService.putBusiness(String.valueOf(orderId));
            log.info("业务完成，submit事务");
            // 回调确认。注意，如果经常在这步出现问题，上一步会造成内存泄漏，这里为简单不做处理。
            msgHandlerService.submit(mqMessage);
        }catch (Exception e){
            // 发生异常则通知消息协调者回滚
            log.error("发生异常，将通知消息处理者rollback");
            msgHandlerService.rollback(mqMessage);
            throw e;
        }
        return "create success";
    }




}
