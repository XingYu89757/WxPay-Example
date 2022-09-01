package com.example.wxpayexample.service;

import com.example.wxpayexample.bean.PayDto;
import com.example.wxpayexample.bean.WxAppletConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 小程序配置表 服务类
 * </p>
 *
 * @author yu.xing
 * @since 2022-09-01
 */
public interface WxAppletConfigService extends IService<WxAppletConfig> {

    /**
     * @Author Yu.Xing
     * @Description 通过agentId获取对应的微信配置信息
     **/
    PayDto getWxAppletConfig(String agentId);

    /**
     * @Author Yu.Xing
     * @Description 通过appId获取配置信息
     **/
    PayDto getWxAppletConfigByAppId(String appId);
}
