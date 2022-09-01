package com.example.wxpayexample.context;

import com.example.wxpayexample.service.PayService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Yu.Xing
 * @Description 使用策略模式选择不同的支付方式
 **/
@Component
public class PayServiceContext {
    private final Map<Integer, PayService> handlerMap = new HashMap<>();

    public PayService getPayService(Integer type) {
        return handlerMap.get(type);
    }

    public void putPayService(Integer code, PayService payService) {
        handlerMap.put(code, payService);
    }

}
