-- ----------------------------
-- Table structure for wx_applet_config
-- ----------------------------
DROP TABLE IF EXISTS `wx_applet_config`;
CREATE TABLE `wx_applet_config`
(
    `id`                     int(10) NOT NULL AUTO_INCREMENT COMMENT '小程序id（与前端约定好的）',
    `agent_id`               varchar(32)  DEFAULT NULL COMMENT '合伙人id',
    `applet_appid`           varchar(100) DEFAULT NULL COMMENT '小程序 appId',
    `applet_secret`          varchar(100) DEFAULT NULL COMMENT '小程序 appSecret',
    `merchant_app_id`        varchar(255) DEFAULT NULL COMMENT '商户号app_id',
    `merchant_api_key`       varchar(255) DEFAULT NULL COMMENT '商户号api_key',
    `apiclient_cert`         varchar(255) DEFAULT NULL COMMENT '商户号退款证书地址',
    `create_time`            datetime     DEFAULT NULL COMMENT '创建时间',
    `update_time`            datetime     DEFAULT NULL COMMENT '更新时间',
    `merchant_apiV3_key`     varchar(40)  DEFAULT NULL COMMENT '微信apiv3密钥',
    `merchant_serial_number` varchar(128) DEFAULT NULL COMMENT '证书序列号',
    `notify_url`             varchar(256) DEFAULT NULL COMMENT '支付回调地址',
    `refund_notify_url`      varchar(256) DEFAULT NULL COMMENT '退款回调地址',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = utf8 COMMENT ='小程序配置表';