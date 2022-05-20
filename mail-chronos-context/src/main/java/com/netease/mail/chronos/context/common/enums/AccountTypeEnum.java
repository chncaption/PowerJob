package com.netease.mail.chronos.context.common.enums;

import com.netease.mail.chronos.context.common.constants.BizParamKeyConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Echo009
 * @since 2022/5/20
 */
@Getter
@AllArgsConstructor
public enum AccountTypeEnum {
    /**
     * 账号类型
     */
    MAIL("MAIL", BizParamKeyConstant.MAIL_ACCOUNT),
    MASTER("MASTER",BizParamKeyConstant.MASTER_ACCOUNT);

    private final String code;

    private final String key;


    public static AccountTypeEnum getByCodeOrDefault(String code) {
        for (AccountTypeEnum value : AccountTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return MAIL;
    }


}
