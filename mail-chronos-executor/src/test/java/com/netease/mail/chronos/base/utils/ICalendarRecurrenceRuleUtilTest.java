package com.netease.mail.chronos.base.utils;

import lombok.SneakyThrows;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.property.ExDate;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


/**
 * @author Echo009
 * @since 2021/10/13
 */
class ICalendarRecurrenceRuleUtilTest {

    @Test
    @SneakyThrows
    void calculateNextTriggerTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date seedTime = simpleDateFormat.parse("2021-09-29 20:00:00");
        Date startTime = simpleDateFormat.parse("2021-09-30 01:00:00");

        long nextTriggerTime = ICalendarRecurrenceRuleUtil.calculateNextTriggerTime("FREQ=MINUTELY;INTERVAL=60;COUNT=24;UNTIL=20210930T010000Z", seedTime.getTime(), startTime.getTime());
        System.out.println(nextTriggerTime);
        System.out.println(simpleDateFormat.format(nextTriggerTime));

        nextTriggerTime = ICalendarRecurrenceRuleUtil.calculateNextTriggerTimeExDateStrList("FREQ=MINUTELY;INTERVAL=60;COUNT=24;UNTIL=20210930T010000Z", seedTime.getTime(), startTime.getTime(),
                Arrays.asList("20210930", "20210929T180000Z","20210930T030000"));
        System.out.println(nextTriggerTime);
        System.out.println(simpleDateFormat.format(nextTriggerTime));

        nextTriggerTime = ICalendarRecurrenceRuleUtil.calculateNextTriggerTimeExDateListWithOffset("FREQ=MINUTELY;INTERVAL=60;COUNT=24;UNTIL=20210930T010000Z", seedTime.getTime(),-60000, startTime.getTime(),
                Arrays.asList("20210930", "20210930T020000","20210930T030000"), "Asia/Shanghai");
        System.out.println(nextTriggerTime);
        System.out.println(simpleDateFormat.format(nextTriggerTime));
    }


    @Test
    @SneakyThrows
    void simpleTest() {
        //https://datatracker.ietf.org/doc/html/rfc5545#section-3.8.5.1
        final ExDate exDate = new ExDate();
        exDate.setValue("19960402T010000Z,19960403T010000Z,19960404T010000Z");
        exDate.setUtc(false);
        DateList dateList = exDate.getDates();
        for (net.fortuna.ical4j.model.Date date : dateList) {
            System.out.println(date.getTime());
        }

    }
}