package com.netease.mail.chronos.executor.config;

import lombok.extern.slf4j.Slf4j;
import org.cylee.commons.httpclient.HttpClientUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Echo009
 * @since 2022/5/20
 */
@Configuration
@Slf4j
public class HttpClientConfig {

    @Bean
    public HttpClientUtil httpClientUtil() {
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        // 暂时先固定配置
        httpClientUtil.setALIVE_TIME_OUT(20000);
        httpClientUtil.setCONNECTION_REQUEST_TIME_OUT(5000);
        httpClientUtil.setCONNECTION_TIME_OUT(5000);
        httpClientUtil.setSO_TIME_OUT(5000);
        httpClientUtil.setMAX_PER_ROUTE(5000);
        httpClientUtil.setMAX_TOTAL(5000);
        httpClientUtil.init();
        return httpClientUtil;
    }


}
