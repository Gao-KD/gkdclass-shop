package org.example.feign;

import org.example.utils.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gkdclass-order-service")
public interface ProductOrderFeignService {

    @GetMapping("/api/order/v1/query_state")
    JsonData queryProductOrderState(@RequestParam("out_trade_no")String outTradeNo);
}
