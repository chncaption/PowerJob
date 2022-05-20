package com.netease.mail.chronos.context.common.constants;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Echo009
 * @since 2022/1/7
 */
@Data
@Accessors(chain = true)
public final class BizParamKeyConstant {
    /**
     * 服务升降级
     */
    public static final String SRV_UPGRADE = "srv-upgrade";
    /**
     * 履约
     */
    public static final String SRV_FULFILL = "srv-fulfill";
    /* 如果相应的业务域内账号参数里没有设置对应的账号参数，那么会根据处理类型取邮箱账号或者大师号标识 */
    /**
     * 邮箱账号标识
     */
    public static final String MAIL_ACCOUNT = "uid";
    /**
     * 大师号标识
     */
    public static final String MASTER_ACCOUNT = "muid";

    private BizParamKeyConstant() {

    }

}
