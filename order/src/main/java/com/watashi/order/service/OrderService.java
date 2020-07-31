package com.watashi.order.service;

import com.watashi.api.Order;
import com.watashi.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderService {
    // 没配数据库，模拟保存就行
    @Autowired
    OrderMapper orderMapper;

    @Transactional
    public void save(Order order){
        // id为-1时模拟出现异常回滚、
        if (order.getOrderId() == -1){
            int i = 1 / 0;
        }
        orderMapper.save(order);
        log.debug("保存了订单："+ order);
    }
}
