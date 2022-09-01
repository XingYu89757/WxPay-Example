package com.example.wxpayexample.context;

import com.example.wxpayexample.common.PaymentCodeHandler;
import com.example.wxpayexample.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Slf4j
@Repository
public class PayServiceListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, Object> beans = event.getApplicationContext().getBeansWithAnnotation(PaymentCodeHandler.class);
        PayServiceContext payServiceContext = event.getApplicationContext().getBean(PayServiceContext.class);
        beans.forEach((name, bean) -> {
            PaymentCodeHandler typeHandler = bean.getClass().getAnnotation(PaymentCodeHandler.class);
            payServiceContext.putPayService(typeHandler.value().getCode(), (PayService) bean);
        });
    }

}
