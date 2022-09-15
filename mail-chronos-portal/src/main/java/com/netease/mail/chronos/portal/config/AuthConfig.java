package com.netease.mail.chronos.portal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.sql.In;

/**
 * @author Echo009
 * @since 2022/5/12
 */
@Configuration
@Getter
@Setter
public class AuthConfig {

    @Value("${auth.config.maxInterval:30000}")
    private Long maxInterval;

    @Value("${auth.config.allowDisableMasterFpCheck:false}")
    private boolean allowDisableMasterFpCheck;

    @Value("${auth.config.allowDisableTimestampCheck:false}")
    private boolean allowDisableTimestampCheck;

    @Value("${auth.config.allowDisableSignCheck:false}")
    private boolean allowDisableSignCheck;



}
