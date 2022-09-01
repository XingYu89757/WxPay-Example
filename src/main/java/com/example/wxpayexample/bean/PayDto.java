package com.example.wxpayexample.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class PayDto implements Serializable {
    //支付类型
    private Integer paymentCode;

    //微信配置属性
    private String appId;//小程序ID
    private String appSecret;//小程序密钥
    private String mchId;//商户号
    private String mchKey;//商户号密钥
    private String apiV3Key;//商户号密钥V3
    private String apiclientCert;//商户号退款证书地址
    private String merchantSerialNumber; //商户API证书的证书序列号

    //支付相关属性
    private String orderId;//订单号
    private String device_info;//设备号
    private String nonce_str;//随机字符串
    private String sign;//签名
    private String body;//商品描述
    private String detail;//商品详情
    private String attach;//附加数据
    private String out_trade_no;//商户订单号
    private String fee_type;//货币类型
    private String spbill_create_ip;//终端IP
    private String time_start;//交易起始时间
    private String time_expire;//交易结束时间
    private String goods_tag;//商品标记
    private Integer total_fee;//总金额
    private String notify_url;//通知地址
    private String trade_type;//交易类型
    private String limit_pay;//指定支付方式
    private String openId;//用户标识

    //退款相关属性
    private String out_refund_no;//商户退款单号
    private String refund_reason;//退款原因
    private String refund_notify_url;//退款结果回调地址
    private String refund_amount;//退款金额

    //提现相关属性
    private String partner_trade_no;//提现单号
    private String amount;//金额
    private String desc;//企业付款到零钱备注

    public void loadWxAppletConfig(WxAppletConfig wxAppletConfig) {
        setAppId(wxAppletConfig.getAppletAppid());
        setMchId(wxAppletConfig.getMerchantAppId());
        setMchKey(wxAppletConfig.getMerchantApiKey());
        setApiV3Key(wxAppletConfig.getMerchantApiv3Key());
        setMerchantSerialNumber(wxAppletConfig.getMerchantSerialNumber());
        setNotify_url(wxAppletConfig.getNotifyUrl());
        setRefund_notify_url(wxAppletConfig.getRefundNotifyUrl());
    }
}
