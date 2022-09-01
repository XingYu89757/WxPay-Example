package com.example.wxpayexample.service;



import com.example.wxpayexample.bean.PayDto;

import java.util.Map;

public interface PayService {
    /**
     * @Description 支付
     * @Author Yu.Xing
     */
    Map pay(PayDto payInfo) throws Exception;

    /**
     * @Description 订单查询
     * @Author Yu.Xing
     */
    Map orderQuery(PayDto payInfo) throws Exception;

    /**
     * @Description 退款
     * @Author Yu.Xing
     */
    Map refund(PayDto payInfo) throws Exception;

    /**
     * @Author Yu.Xing
     * @Description 退款订单查询查询
     */
    Map refundOrderQuery(PayDto payInfo) throws Exception;

    /**
     * @Author Yu.Xing
     * @Description 企业付款到零钱（提现）
     **/
    Map transfers(PayDto payInfo)throws Exception;
}
