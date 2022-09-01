package com.example.wxpayexample.controller;

import cn.hutool.core.net.Ipv4Util;
import com.example.wxpayexample.bean.PayDto;
import com.example.wxpayexample.bean.PayPojo;
import com.example.wxpayexample.context.PayServiceContext;
import com.example.wxpayexample.service.PayService;
import com.example.wxpayexample.service.WxAppletConfigService;
import com.example.wxpayexample.utils.IpUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class WxPayController {

    @Resource
    private PayServiceContext payServiceContext;

    @Resource
    private WxAppletConfigService wxAppletConfigService;

    /**
     * @Author Yu.Xing
     * @Description 使用demo,退款，提现相同调用方式，需要改变一下请求参数
     **/
    @ApiOperation(httpMethod = "POST", value = "支付接口", produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/wxPay")
    public Map wxPay(HttpServletRequest request, @RequestBody PayPojo payPojo) {
        PayService payService = payServiceContext.getPayService(payPojo.getPaymentCode());
        //从token中获取到登录人信息，从登录人信息中判断所属商户,此Demo省略相关逻辑，默认使用一个商户
        PayDto payDto = wxAppletConfigService.getWxAppletConfig("89757");
        payDto.setTotal_fee(payPojo.getAmount());
        payDto.setOrderId(payPojo.getOrderId());
        payDto.setOpenId(payPojo.getOpenId());
        payDto.setBody("testPay");
        payDto.setSpbill_create_ip(IpUtil.getIp(request));
        try {
            Map pay = payService.pay(payDto);
            return pay;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
