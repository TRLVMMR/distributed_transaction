package com.watashi.msghandler.service;

//import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(value = "transaction-order")
public interface ProviderService {
    @PostMapping("/rpc/confirm")
    public boolean confirm(@RequestBody String msgId);

    @PostMapping("/rpc/deleteBusiness")
    public String deleteBusiness(@RequestBody String msgId);
}
