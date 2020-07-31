package com.watashi.order.mapper;

import com.watashi.api.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    @Insert("insert into `order`(id, content) values(#{orderId}, #{content});")
    void save(Order order);
}
