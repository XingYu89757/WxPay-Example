package com.example.wxpayexample.controller;

import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.example.wxpayexample.bean.PayDto;
import com.example.wxpayexample.service.WxAppletConfigService;
import com.example.wxpayexample.utils.WxPayV2Util;
import com.example.wxpayexample.utils.WxPayV3Util;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.notification.Notification;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationHandler;
import com.wechat.pay.contrib.apache.httpclient.notification.NotificationRequest;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/wxpay")
@Api(tags = "支付回调接口")
public class CallBackController {

    @Resource
    private WxAppletConfigService wxAppletConfigService;

    /**
     * @Author Yu.Xing
     * @Description 支付回调V2版本
     * 参考链接：https://pay.weixin.qq.com/wiki/doc/api/wxa/wxa_api.php?chapter=9_7&index=8
     **/
    @RequestMapping("/paymentCallBackV2")
    @ResponseBody
    public void paymentCallBackV2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> map = WxPayV2Util.notifyUrl(request);
            if (!CollectionUtils.isEmpty(map)) {
                if ("SUCCESS".equals(map.get("return_code"))) {
                    String out_trade_no = map.get("out_trade_no").toString();
                    if ("SUCCESS".equals(map.get("result_code"))) {
                        //调用自己的处理逻辑
                        log.info("out_trade_no is {}", out_trade_no);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("支付回调异常", e);
        } finally {
            log.error("微信返回值");
            response.setContentType("text/xml");
            //给微信服务器返回 成功标示 否则会一直询问 咱们服务器 是否回调成功
            PrintWriter writer = response.getWriter();
            //封装 返回值
            StringBuffer buffer = new StringBuffer();
            buffer.append("<xml>");
            buffer.append("<return_code><![CDATA[SUCCESS]]></return_code>");
            buffer.append("<return_msg><![CDATA[OK]]></return_msg>");
            buffer.append("</xml>");
            //返回  　　　　　　　
            writer.print(buffer);
        }
    }

    /**
     * @Author Yu.Xing
     * @Description 支付回调V3版本
     * 参考链接：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_5.shtml
     * 因为存在多个商户，并且使用的同一个回调地址，因此需要区分回调具体属于那个商户，才可以获取到对应商户的商户平台证书
     *
     **/
    @PostMapping("/paymentCallBackV3/{appId}")
    @ResponseBody
    public void paymentCallBackV3(HttpServletRequest httpServletRequest, HttpServletResponse response,
                                  @PathVariable(value = "appId") String appId) {
        Map<String, String> map = new HashMap<>(12);
        try {
            //获取回调请求头中的数据
            String timestamp = httpServletRequest.getHeader("Wechatpay-Timestamp");//应答时间戳
            String nonce = httpServletRequest.getHeader("Wechatpay-Nonce");//应答随机串
            String serialNo = httpServletRequest.getHeader("Wechatpay-Serial");//微信支付的平台证书序列号
            String signature = httpServletRequest.getHeader("Wechatpay-Signature");//应答签名
            String requestBody = WxPayV3Util.getRequestBody(httpServletRequest);
            log.info("timestamp:{} nonce:{} serialNo:{} signature:{} requestBody:{}", timestamp, nonce, serialNo, signature, requestBody);
            // 构建request，传入必要参数,用于解密回调信息
            NotificationRequest request = new NotificationRequest.Builder().withSerialNumber(serialNo)
                    .withNonce(nonce)
                    .withTimestamp(timestamp)
                    .withSignature(signature)
                    .withBody(requestBody)
                    .build();
            log.info("NotificationRequest:{}", request.toString());
            //通过平台证书序列号查询，商户对应的信息
            PayDto payDto = wxAppletConfigService.getWxAppletConfigByAppId(appId);
            log.info("获取商户对应的信息：{}", payDto);
            //获取对应商户的验签器
            Verifier verifier = WxPayV3Util.getVerifier(payDto.getMchId());
            NotificationHandler handler = new NotificationHandler(verifier, payDto.getApiV3Key().getBytes(StandardCharsets.UTF_8));
            // 验签和解析请求体
            Notification notification = handler.parse(request);
            // 从notification中获取解密报文
            String decryptData = notification.getDecryptData();
            log.info("获取到的解密报文:{}", decryptData);
            if (StringUtils.isNotEmpty(decryptData) && StringUtils.equals(notification.getEventType(), "TRANSACTION.SUCCESS")) {
                JSONObject jsonObject = JSONObject.parseObject(decryptData);
                String outTradeNo = jsonObject.getString("out_trade_no");

                //调自己的处理逻辑
                log.info("outTradeNo is {}", outTradeNo);

                response.setStatus(200);
                map.put("code", "SUCCESS");
                map.put("message", "SUCCESS");
            } else {
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "验签错误");
            }
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            response.setStatus(500);
            map.put("code", "ERROR");
            map.put("message", "验签错误");
            throw new RuntimeException(e);
        } finally {
            response.setHeader("Content-type", ContentType.JSON.toString());
            try {
                response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
                response.flushBuffer();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
