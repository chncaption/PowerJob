package com.netease.mail.chronos.base.utils;

import cn.hutool.core.collection.CollUtil;
import com.netease.mail.chronos.base.enums.BaseStatusEnum;
import com.netease.mail.chronos.base.exception.BaseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Echo009
 * @since 2021/9/29
 * <p>
 * http://ical4j.github.io/
 *
 * 备注：这里都是基于 UNIX 时间戳进行计算的，不需要额外考虑时区 （和客户端协商好的，传的都是 UTC 时间）
 */
@Slf4j
public class ICalendarRecurrenceRuleUtil {

    private ICalendarRecurrenceRuleUtil() {

    }

    /**
     * 计算下次触发时间，支持 EXDATE
     *
     * @param recurrenceRule 重复规则
     * @param seedTime       种子时间
     * @param startTime      开始时间
     * @param exDateList     排除的日期/时间列表
     * @param tzId           时区
     * @return 下次触发时间，会跳过 exDateStr 指定的日期/时间
     */
    @SneakyThrows
    public static long calculateNextTriggerTimeExDateList(String recurrenceRule, long seedTime, long startTime, List<String> exDateList, String tzId) {
        long nextTriggerTime = calculateNextTriggerTime(recurrenceRule, seedTime, startTime);
        if (nextTriggerTime == 0L || CollUtil.isEmpty(exDateList)) {
            return nextTriggerTime;
        }
        final List<Date> dates = toDateList(exDateList, tzId);
        // 参考 net.fortuna.ical4j.model.Component.calculateRecurrenceSet 中的处理逻辑
        while (dates.contains(new DateTime(nextTriggerTime)) || dates.contains(new Date(nextTriggerTime))) {
            nextTriggerTime = calculateNextTriggerTime(recurrenceRule, seedTime, nextTriggerTime);
        }
        return nextTriggerTime;
    }



    public static List<net.fortuna.ical4j.model.Date> toDateList(List<String> exDateList, String tzId){

        try {
            List<net.fortuna.ical4j.model.Date> dateList = new LinkedList<>();
            for (String str : exDateList) {
                // 这里简单判断一下
                if (StringUtils.contains(str, 'T') && StringUtils.contains(str, 'Z')) {
                    // 如果是 UTC 时间则不带时区
                    dateList.addAll(new DateList(str, Value.DATE_TIME));
                } else if (StringUtils.contains(str, 'T') && !StringUtils.contains(str, 'Z')) {
                    // 如果非 UTC 时间（带 T 不带 Z），则转成 UTC 时间
                    final String s = TimeUtil.toUtcDateTimeString(str, StringUtils.isBlank(tzId)? TimeZone.getDefault().getID() : tzId);
                    dateList.addAll(new DateList(s, Value.DATE_TIME));
                } else {
                    // 如果是日期类型 （即不带 T）
                    final String s = TimeUtil.toUtcDateString(str, StringUtils.isBlank(tzId)? TimeZone.getDefault().getID() : tzId);
                    dateList.addAll(new DateList(str, Value.DATE));
                }
            }
            return dateList;
        }catch (Exception e){
            log.warn("[opt:toDateList,exDateList:{},tzId:{},msg:failed,error:{}]",exDateList,tzId,e.getMessage(),e);
            throw new BaseException(BaseStatusEnum.ILLEGAL_ARGUMENT.getCode(), "解析 ExDate 失败 " + exDateList);
        }
    }

    /**
     * 计算下次触发时间，支持 EXDATE
     *
     * @param recurrenceRule 重复规则
     * @param seedTime       种子时间
     * @param startTime      开始时间
     * @param exDateStrList  排除的日期/时间列表 （一定是 UTC 时间）
     * @return 下次触发时间，会跳过 exDateStr 指定的日期/时间
     */
    @SneakyThrows
    public static long calculateNextTriggerTimeExDateStrList(String recurrenceRule, long seedTime, long startTime, List<String> exDateStrList) {
        long nextTriggerTime = calculateNextTriggerTime(recurrenceRule, seedTime, startTime);
        if (nextTriggerTime == 0L || CollUtil.isEmpty(exDateStrList)) {
            return nextTriggerTime;
        }
        List<Date> dateList = new LinkedList<>();
        for (String str : exDateStrList) {
            // 这里需要兼容 DateTime 和 Date 两种格式
            if (StringUtils.contains(str, 'T')) {
                // 如果是 UTC 时间
                dateList.addAll(new DateList(str, Value.DATE_TIME));
            } else {
                // 如果是日期类型 （即不带 T )
                dateList.addAll(new DateList(str, Value.DATE));
            }
        }
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
