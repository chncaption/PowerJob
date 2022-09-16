package com.netease.mail.chronos.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Echo009
 * @since 2022/9/14
 */
@Slf4j
public class SignUtil {

    private static final String SP_KEY = "_";


    public static String genSign(List<String> keys){
        final String key = String.join(SP_KEY, keys);
        return DigestUtils.md5Hex(key.getBytes(StandardCharsets.UTF_8));
    }

}
