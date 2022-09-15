package com.netease.mail.chronos.portal.enums;

import com.netease.mail.chronos.base.enums.StatusEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Echo009
 * @since 2022/9/15
 */
@Getter
@RequiredArgsConstructor
public enum ExternalTaskBaseStatusEnum implements StatusEnum {
    /**
     * 状态枚举
     */
    SUCCESS(200, "成功"),
    TOO_FREQUENTLY(203, "请求过于频繁"),
    ILLEGAL_ARGUMENT(400, "参数错误"),
    ILLEGAL_ACCESS(401, "非法访问"),
    TOO_MANY_TASK(402, "创建的任务数量超限"),
    UNKNOWN(500, "服务器异常");

    private final Integer code;

    private final String desc;


}