package com.example.wxpayexample.enums;

import lombok.Getter;

public class OrderEnums {

    @Getter
    public enum OrderStateEnum implements CommonEnum {
        TO_BE_PAID(1, "未付款"),
        TO_BE_CONFIRMED(2, "待确认"),
        TO_BE_SHIPPED(3, "待发货"),
        SHIPPED(4, "已发货"),
        COMPLETED(5, "已完成"),
        CANCELED(6, "取消");
        private Integer code;
        private String msg;

        OrderStateEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    @Getter
    public enum PaymentCodeEnum implements CommonEnum{
        APPLET_WXPAY_V2(1, "Applet微信V2"), // 小程序 有用到
        APPLET_WXPAY_V3(2, "Applet微信V3");
        private Integer code;
        private String msg;

        PaymentCodeEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }


}
