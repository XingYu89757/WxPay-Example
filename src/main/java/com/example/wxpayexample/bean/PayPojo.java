package com.example.wxpayexample.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PayPojo",description = "支付参数")
public class PayPojo {

    @ApiModelProperty(value = "支付类型:{1:Applet微信V2, 2:Applet微信V3}")
    private Integer paymentCode;

    @ApiModelProperty(value = "金额（单位：分）")
    private Integer amount;

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "微信小程序openId")
    private String openId;

}
