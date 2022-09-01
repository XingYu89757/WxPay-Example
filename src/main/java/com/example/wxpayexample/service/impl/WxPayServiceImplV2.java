package com.example.wxpayexample.service.impl;

import com.example.wxpayexample.common.PaymentCodeHandler;
import com.example.wxpayexample.enums.OrderEnums;
import com.example.wxpayexample.bean.PayDto;
import com.example.wxpayexample.service.PayService;
import com.example.wxpayexample.utils.WxPayV2Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Slf4j
@Service
@PaymentCodeHandler(value = OrderEnums.PaymentCodeEnum.APPLET_WXPAY_V2)
public class WxPayServiceImplV2 implements PayService {
    @Override
    public Map pay(PayDto payInfo) throws Exception {
        SortedMap<Object, Object> sortedMap = new TreeMap<>();
        WxPayV2Util wxPayV2Util = new WxPayV2Util(payInfo);
        //调用支付方法
        String pay = wxPayV2Util.pay();
        log.info("pay response info is {}", payInfo);
        Map responseMap = wxPayV2Util.doXMLParse(pay);
        log.info("pay response info change map is {}", responseMap.toString());
        String return_code = responseMap.get("return_code").toString();//返回状态码
        String return_msg = responseMap.get("return_msg").toString();//返回信息
        if ("SUCCESS".equals(return_code)) {
            String prepay_id = responseMap.get("prepay_id").toString();//返回的预付单信息
            sortedMap.put("nonceStr", wxPayV2Util.getNonceStr());//随机数
            sortedMap.put("package", "prepay_id=" + prepay_id);//数据包
            Long timeStamp = System.currentTimeMillis() / 1000;
            sortedMap.put("timeStamp", timeStamp + "");//时间戳
            //再次签名,生成paySign,返回前端用于小程序调起微信支付
            String paySign = wxPayV2Util.createSign("UTF-8", sortedMap);
            sortedMap.put("paySign", paySign);//签名信息
            sortedMap.put("appId", payInfo.getAppId());//小程序对应的appId
            sortedMap.put("signType", "MD5");//签名类型
        }
        return responseMap;
    }

    @Override
    public Map orderQuery(PayDto payInfo) throws Exception {
        WxPayV2Util wxPayV2Util = new WxPayV2Util(payInfo);
        //调用查询订单方法
        String pay = wxPayV2Util.orderQuery();
        Map responseMap = wxPayV2Util.doXMLParse(pay);
        return responseMap;
    }

    @Override
    public Map refund(PayDto payInfo) throws Exception {
        WxPayV2Util wxPayV2Util = new WxPayV2Util(payInfo);
        //调用查询订单方法
        String pay = wxPayV2Util.refund();
        Map responseMap = wxPayV2Util.doXMLParse(pay);
        return responseMap;
    }

    @Override
    public Map refundOrderQuery(PayDto payInfo) throws Exception {
        return this.orderQuery(payInfo);
    }

    @Override
    public Map transfers(PayDto payInfo) throws Exception {
        WxPayV2Util wxPayV2Util = new WxPayV2Util(payInfo);
        String pay = wxPayV2Util.transfers();
        Map responseMap = wxPayV2Util.doXMLParse(pay);
        if ("SUCCESS".equals(responseMap.get("result_code"))) {
            if ("SUCCESS".equals(responseMap.get("return_code"))) {
                String partnerTradeNo = responseMap.get("partner_trade_no").toString();//商户订单号
                String paymentNo = responseMap.get("payment_no").toString();//微信付款单号
                String paymentTime = responseMap.get("payment_time").toString();//付款成功时间
                log.info("partnerTradeNo is {},paymentNo is {},paymentTime is{}", partnerTradeNo, paymentNo, paymentTime);
            } else {
                throw new RuntimeException("提现失败");
            }
        } else {
            log.error("err_code_des:" + responseMap.get("err_code_des"));
            throw new RuntimeException(responseMap.get("err_code_des").toString());
        }
        return responseMap;
    }
}
