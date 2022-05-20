package com.netease.mail.chronos.executor.fulfill.util;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;

/**
 * @author gzlicanyi
 * 从履约拷贝过来，说实话有点搞
 */
public class VerifyUtil {

    private static final String SALT_IN_SALT = "sVJfcvVydJfF";


    public static Map<String, String> getVerifyParamsBySalt(String salt) {
        return getVerifyParamsCore(salt);
    }

    /**
     * 根据配置的生成时间去生成校验参数
     */
    public static Map<String, String> getVerifyParamsByCreateTime(long extStandardCreateTime) {
        String salt = getSalt(extStandardCreateTime);
        return getVerifyParamsCore(salt);
    }


    private static String getSalt(long extStandardCreateTime) {
        return DigestUtils.md5Hex(extStandardCreateTime + "_" + SALT_IN_SALT);
    }

    private static Map<String, String> getVerifyParamsCore(String salt) {
        String fulfillVerifyNonStr = RandomStringUtils.randomAlphanumeric(12);
        String fulfillVerifyTime = String.valueOf(System.currentTimeMillis());
        String fulfillVerifySign = DigestUtils.md5Hex(salt + fulfillVerifyNonStr + fulfillVerifyTime);
        return ImmutableMap.<String, String>builder().put("fulfillVerifyNonStr", fulfillVerifyNonStr)
                .put("fulfillVerifyTime", fulfillVerifyTime).put("fulfillVerifySign", fulfillVerifySign).build();
    }
}
