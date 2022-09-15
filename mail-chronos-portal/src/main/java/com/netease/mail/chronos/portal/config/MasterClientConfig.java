package com.netease.mail.chronos.portal.config;

import com.netease.mail.master.MasterfpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Echo009
 * @since 2022/9/14
 */
@Configuration
@Slf4j
public class MasterClientConfig {

    @Bean
    public MasterfpUtils initMasterfp(
            @Value("${master.core.host}") String masterCoreHost,
            @Value("${master.core.appId}") String masterCoreAppid,
            @Value("${master.core.salt}") String masterCoreSalt
    ) {
        log.info("[cmd:init initMasterfpUtil,masterCoreHost:{},masterCoreAppid:{},masterCoreSalt:{}]",masterCoreHost,masterCoreAppid,masterCoreSalt);
        return MasterfpUtils.newInitializer()
                .host(masterCoreHost)
                .salt(masterCoreSalt)
                .appid(masterCoreAppid)
                .init();
    }

}
