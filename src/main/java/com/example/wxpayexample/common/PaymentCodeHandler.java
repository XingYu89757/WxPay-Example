package com.example.wxpayexample.common;

import com.example.wxpayexample.enums.OrderEnums;

import java.lang.annotation.*;

/**
 * @Author Yu.Xing
 * @Description 自定义注解，标识不同的支付类型
 **/
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PaymentCodeHandler {
    OrderEnums.PaymentCodeEnum value();
}
