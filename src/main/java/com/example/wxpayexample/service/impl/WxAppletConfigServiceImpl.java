package com.example.wxpayexample.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.wxpayexample.bean.PayDto;
import com.example.wxpayexample.bean.WxAppletConfig;
import com.example.wxpayexample.mapper.WxAppletConfigMapper;
import com.example.wxpayexample.service.WxAppletConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 小程序配置表 服务实现类
 * </p>
 *
 * @author yu.xing
 * @since 2022-09-01
 */
@Slf4j
@Service
public class WxAppletConfigServiceImpl extends ServiceImpl<WxAppletConfigMapper, WxAppletConfig> implements WxAppletConfigService {

    @Override
    public PayDto getWxAppletConfig(String agentId) {
        if (StringUtils.isEmpty(agentId)) {
            return null;
        }
        QueryWrapper<WxAppletConfig> wxAppletConfigQueryWrapper = new QueryWrapper<>();
        wxAppletConfigQueryWrapper.lambda()
                .eq(WxAppletConfig::getAgentId, agentId);
        WxAppletConfig wxAppletConfig = super.baseMapper.selectOne(wxAppletConfigQueryWrapper);
        if (wxAppletConfig == null) {
            return null;
        }
        PayDto payDto = new PayDto();
        payDto.setAppId(wxAppletConfig.getAppletAppid());
        payDto.setAppSecret(wxAppletConfig.getAppletSecret());
        payDto.setMchId(wxAppletConfig.getMerchantAppId());
        payDto.setMchKey(wxAppletConfig.getMerchantApiKey());
        payDto.setNotify_url(wxAppletConfig.getNotifyUrl());
        payDto.setRefund_notify_url(wxAppletConfig.getRefundNotifyUrl());
        payDto.setApiV3Key(wxAppletConfig.getMerchantApiv3Key());
        payDto.setMerchantSerialNumber(wxAppletConfig.getMerchantSerialNumber());
        return payDto;
    }

    @Override
    public PayDto getWxAppletConfigByAppId(String appId) {
        if (org.springframework.util.StringUtils.isEmpty(appId)) {
            log.error("appId为空");
            return null;
        }
        WxAppletConfig wxAppletConfig = baseMapper.selectOne(new QueryWrapper<WxAppletConfig>().eq(WxAppletConfig.APPLET_APPID, appId));
        if (wxAppletConfig == null) {
            log.error("未通过appId{}，查询到配置信息", appId);
        }
        PayDto payDto = new PayDto();
        payDto.loadWxAppletConfig(wxAppletConfig);
        log.info("获取到的配置信息为：{}", payDto);
        return payDto;
    }
}
