package com.watashi.pay.mapper;

import com.watashi.api.Pay;
import org.apache.ibatis.annotations.Insert;

public interface PayMapper {

    @Insert("insert into `pay`(order_id) values(#{orderId});")
    void save(Pay pay);
}
