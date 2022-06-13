package com.netease.mail.chronos.base.utils;

import com.netease.mail.chronos.base.enums.BaseStatusEnum;
import com.netease.mail.chronos.base.exception.BaseException;
import lombok.SneakyThrows;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.property.ExDate;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Echo009
 * @since 2021/9/29
 * <p>
 * http://ical4j.github.io/
 *
 * 备注：这里都是基于 UNIX 时间戳进行计算的，不需要额外考虑时区 （和客户端协商好的，传的都是 UTC 时间）
 */
public class ICalendarRecurrenceRuleUtil {

    private ICalendarRecurrenceRuleUtil() {

    }

    /**
     * 计算下次触发时间，支持 EXDATE
     *
     * @param recurrenceRule 重复规则
     * @param seedTime       种子时间
     * @param startTime      开始时间
     * @param exDateStr      排除的日期/时间列表
     * @return 下次触发时间，会跳过 exDateStr 指定的日期/时间
     */
    @SneakyThrows
    public static long calculateNextTriggerTime(String recurrenceRule, long seedTime, long startTime, String exDateStr) {
        long nextTriggerTime = calculateNextTriggerTime(recurrenceRule, seedTime, startTime);
        if (nextTriggerTime == 0L || StringUtils.isBlank(exDateStr)) {
            return nextTriggerTime;
        }
        // 计算出列表
        final ExDate exDate = new ExDate();
        exDate.setValue(exDateStr);
        final List<Date> dateList = new ArrayList<>(exDate.getDates());
        // 参考 net.fortuna.ical4j.model.Component.calculateRecurrenceSet 中的处理逻辑
        while (dateList.contains(new DateTime(nextTriggerTime)) || dateList.contains(new Date(nextTriggerTime))) {
            nextTriggerTime = calculateNextTriggerTime(recurrenceRule, seedTime, nextTriggerTime);
        }
        return nextTriggerTime;
    }

    public static long calculateNextTriggerTime(String recurrenceRule, long seedTime, long startTime) {
        Recur recur = construct(recurrenceRule);
        net.fortuna.ical4j.model.Date nextDate = recur.getNextDate(new DateTime(seedTime), new DateTime(startTime));
        return nextDate == null ? 0L : nextDate.getTime();
    }


    public static Recur construct(String recurrenceRule) {
        try {
            return new Recur(recurrenceRule);
        } catch (ParseException parseException) {
            throw new BaseException(BaseStatusEnum.ILLEGAL_ARGUMENT.getCode(), "无效的日历重复规则" + recurrenceRule);
        }
    }

}
