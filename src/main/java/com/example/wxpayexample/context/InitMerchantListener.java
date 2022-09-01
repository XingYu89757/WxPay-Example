package com.example.wxpayexample.context;

import com.example.wxpayexample.bean.WxAppletConfig;
import com.example.wxpayexample.service.WxAppletConfigService;
import com.example.wxpayexample.utils.WxPayV3Util;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.exception.HttpCodeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.List;

/**
 * @Author Yu.Xing
 * @Description 初始化证书管理器
 **/
@Slf4j
@Repository
public class InitMerchantListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private WxAppletConfigService wxAppletConfigService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        CertificatesManager certificatesManager = WxPayV3Util.getCertificatesManager();
        log.info("初始化微信平台证书");
        //查询所有的商户配置信息
        List<WxAppletConfig> wxAppletConfigs = wxAppletConfigService.list();
        if (!CollectionUtils.isEmpty(wxAppletConfigs)) {
            wxAppletConfigs.forEach(item -> {
                PrivateKey privateKey = WxPayV3Util.loadPrivateKey(item.getMerchantAppId());
                try {
                    certificatesManager.putMerchant(item.getMerchantAppId(), new WechatPay2Credentials(item.getMerchantAppId(),
                            new PrivateKeySigner(item.getMerchantSerialNumber(), privateKey)), item.getMerchantApiv3Key().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                } catch (HttpCodeException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
