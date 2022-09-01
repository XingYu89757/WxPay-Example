package com.example.wxpayexample.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 小程序配置表
 * </p>
 *
 * @author yu.xing
 * @since 2022-09-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wx_applet_config")
@ApiModel(value="WxAppletConfig对象", description="小程序配置表")
public class WxAppletConfig implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "小程序id（与前端约定好的）")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "合伙人id")
    @TableField("agent_id")
    private String agentId;

    @ApiModelProperty(value = "小程序 appId")
    @TableField("applet_appid")
    private String appletAppid;

    @ApiModelProperty(value = "小程序 appSecret")
    @TableField("applet_secret")
    private String appletSecret;

    @ApiModelProperty(value = "商户号app_id")
    @TableField("merchant_app_id")
    private String merchantAppId;

    @ApiModelProperty(value = "商户号api_key")
    @TableField("merchant_api_key")
    private String merchantApiKey;

    @ApiModelProperty(value = "商户号退款证书地址")
    @TableField("apiclient_cert")
    private String apiclientCert;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "微信apiv3密钥")
    @TableField("merchant_apiV3_key")
    private String merchantApiv3Key;

    @ApiModelProperty(value = "证书序列号")
    @TableField("merchant_serial_number")
    private String merchantSerialNumber;

    @ApiModelProperty(value = "支付回调地址")
    @TableField("notify_url")
    private String notifyUrl;

    @ApiModelProperty(value = "退款回调地址")
    @TableField("refund_notify_url")
    private String refundNotifyUrl;


    public static final String ID = "id";

    public static final String AGENT_ID = "agent_id";

    public static final String APPLET_APPID = "applet_appid";

    public static final String APPLET_SECRET = "applet_secret";

    public static final String MERCHANT_APP_ID = "merchant_app_id";

    public static final String MERCHANT_API_KEY = "merchant_api_key";

    public static final String APICLIENT_CERT = "apiclient_cert";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    public static final String MERCHANT_APIV3_KEY = "merchant_apiV3_key";

    public static final String MERCHANT_SERIAL_NUMBER = "merchant_serial_number";

    public static final String NOTIFY_URL = "notify_url";

    public static final String REFUND_NOTIFY_URL = "refund_notify_url";

}
