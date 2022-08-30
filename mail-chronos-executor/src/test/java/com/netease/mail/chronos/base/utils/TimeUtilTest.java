package com.netease.mail.chronos.base.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Echo009
 * @since 2022/8/30
 */
class TimeUtilTest {

    @Test
    @SneakyThrows
    void toUtcDateTimeStr(){
        Assertions.assertEquals("20210928T160000Z",TimeUtil.toUtcDateTimeString("20210929T000000","Asia/Shanghai"));
    }

    @Test
    @SneakyThrows
    void toUtcDateStr(){
        Assertions.assertEquals("20210928",TimeUtil.toUtcDateString("20210929","Asia/Shanghai"));
    }

}

