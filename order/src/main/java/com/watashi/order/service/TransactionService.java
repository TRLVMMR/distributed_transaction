package com.watashi.order.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class TransactionService {
    // 用一个本地set来放业务标记，当存在时，代表提供者某业务完成，消息协调者还不知道。当提供者挂了的时候，这个一起挂。
    Set<String> businessSet = new CopyOnWriteArraySet<>();

    public boolean existBusiness(String businessId){
        return businessSet.contains(businessId);
    }

    public void deleteBusiness(String businessId){
        businessSet.remove(businessId);
    }

    public void putBusiness(String businessId){
        businessSet.add(businessId);
    }
}
