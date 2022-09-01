package com.example.wxpayexample.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.wxpayexample.common.PaymentCodeHandler;
import com.example.wxpayexample.enums.OrderEnums;
import com.example.wxpayexample.bean.PayDto;
import com.example.wxpayexample.service.PayService;
import com.example.wxpayexample.utils.WxPayV3Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@PaymentCodeHandler(value = OrderEnums.PaymentCodeEnum.APPLET_WXPAY_V3)
public class WxPayServiceImplV3 implements PayService {

    @Override
    public Map pay(PayDto payInfo) throws Exception {
        Map<String, Object> result = new HashMap<>();

        WxPayV3Util wxPayV3Util = new WxPayV3Util(payInfo);
        String s = wxPayV3Util.payOrder();
        if (StringUtils.isEmpty(s)) {
            JSONObject jsonObject = JSONObject.parseObject(s);
            String prepay_id = jsonObject.getString("prepay_id");
            //如果prepay_id不为null，说明调用支付接口成功
            if (StringUtils.isNotEmpty(prepay_id)) {
                String nonceStr = wxPayV3Util.getNonceStr();//
                Long timeStamp = System.currentTimeMillis() / 1000;
                result.put("appId", payInfo.getAppId());
                result.put("package", "prepay_id=" + prepay_id);
                result.put("nonceStr", nonceStr);
                result.put("timeStamp", timeStamp.toString());
                result.put("signType", "RSA");
                String appletToken = wxPayV3Util.getAppletToken(payInfo.getAppId(), prepay_id, nonceStr, timeStamp);
                result.put("paySign", appletToken);
            } else {
                String message = jsonObject.getString("message");
                log.error("支付失败,失败原因:{}", message);
                throw new RuntimeException("支付失败");
            }
        }
        return result;
    }

    @Override
    public Map orderQuery(PayDto payInfo) throws Exception {
        Map<String, Object> result = new HashMap<>();
        WxPayV3Util wxPayV3Util = new WxPayV3Util(payInfo);
        try {
            String s = wxPayV3Util.queryOrder();
            if (StringUtils.isEmpty(s)) {
                JSONObject jsonObject = JSONObject.parseObject(s);
                //根据业务自行获取具体信息
                String out_trade_no = jsonObject.getString("out_trade_no");
                String transaction_id = jsonObject.getString("transaction_id");
                String trade_state = jsonObject.getString("trade_state");
                result.put("trade_state", trade_state);
                result.put("out_trade_no", out_trade_no);
                result.put("transaction_id", transaction_id);
                log.info("out_trade_no is  {}, trade is {}", out_trade_no, trade_state);
            }
        } catch (Exception e) {
            throw new RuntimeException("查询订单失败");
        }
        return result;
    }

    @Override
    public Map refund(PayDto payInfo) throws Exception {
        Map<String, Object> result = new HashMap<>();
        WxPayV3Util wxPayV3Util = new WxPayV3Util(payInfo);
        try {
            String refund = wxPayV3Util.refund();
            if (StringUtils.isNotEmpty(refund)) {
                JSONObject jsonObject = JSONObject.parseObject(refund);
                //根据业务自行获取具体信息
                String out_refund_no = jsonObject.getString("out_refund_no");
                String refund_id = jsonObject.getString("refund_id");
                String transaction_id = jsonObject.getString("transaction_id");
                String status = jsonObject.getString("status");
                result.put("out_refund_no", out_refund_no);
                result.put("refund_id", refund_id);
                result.put("status", status);
                result.put("transaction_id", transaction_id);
                log.info("out_refund_no is  {}, status is {}", out_refund_no, status);
            }
        } catch (Exception e) {
            throw new RuntimeException("申请退款失败");
        }
        return result;
    }

    @Override
    public Map refundOrderQuery(PayDto payInfo) throws Exception {
        return this.orderQuery(payInfo);
    }

    @Override
    public Map transfers(PayDto payInfo) throws Exception {
        Map<String, Object> result = new HashMap<>();
        WxPayV3Util wxPayV3Util = new WxPayV3Util(payInfo);
        try {
            String transfer = wxPayV3Util.transfer();
            if (StringUtils.isNotEmpty(transfer)) {
                JSONObject jsonObject = JSONObject.parseObject(transfer);
                String partnerTradeNo = jsonObject.getString("out_batch_no");//商户系统内部的商家批次单号
                String paymentNo = jsonObject.getString("batch_id");//微信批次单号
                String paymentTime = jsonObject.getString("create_time");//批次受理成功时时间
                result.put("partnerTradeNo", partnerTradeNo);
                result.put("paymentNo", paymentNo);
                result.put("paymentTime", paymentTime);
                log.info("partnerTradeNo is {},paymentNo is {},paymentTime is {}", partnerTradeNo, paymentNo, paymentTime);
                //如果微信批次单号不为空，说明提现成功
                if (StringUtils.isEmpty(paymentNo)) {
                    String code = jsonObject.getString("code");
                    String message = jsonObject.getString("message");
                    log.error("err_code:{},err_code_des:{}", code, message);
                    throw new RuntimeException(message);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("申请提现失败");
        }
        return result;
    }
}
