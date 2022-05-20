package com.netease.mail.chronos.context.srvupgrade;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Echo009
 * @since 2022/1/6
 */
@Data
@Accessors(chain = true)
public class ApiParam {

    private String token;

    private Integer strategy;

    private String resource;
    /**
     * 需要处理的账号，非必填
     */
    private String account;
    /**
     * only for commit
     */
    private String extInfo;
    /**
     * only for rollback
     */
    private String commitToken;

}
