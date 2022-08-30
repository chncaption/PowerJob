package com.netease.mail.chronos.base.utils;

import com.netease.mail.chronos.base.enums.BaseStatusEnum;
import com.netease.mail.chronos.base.exception.BaseException;
import lombok.SneakyThrows;
import lombok.val;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Echo009
 * @since 2021/9/23
 */
public class TimeUtil {


    private static final ThreadLocal<SimpleDateFormat> COMMON_DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    private static final ThreadLocal<SimpleDateFormat> NUMBER_DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));

    private static final ThreadLocal<SimpleDateFormat> UTC_DATETIME_FORMAT =
            ThreadLocal.withInitial(() -> {
                final SimpleDateFormat utcFmt = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
                utcFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                return utcFmt;
            });

    private static final ThreadLocal<SimpleDateFormat> UTC_DATE_FORMAT =
            ThreadLocal.withInitial(() -> {
                final SimpleDateFormat utcFmt = new SimpleDateFormat("yyyyMMdd");
                utcFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                return utcFmt;
            });

    private static final ThreadLocal<SimpleDateFormat> T_DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd'T'HHmmss"));

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));

    private TimeUtil() {

    }

    public static String toUtcDateTimeString(String tDateTimeStr , String zoneId){
        try {
            final SimpleDateFormat tDf = T_DATE_FORMAT.get();
            tDf.setTimeZone(TimeUtil.getTimeZoneByZoneId(zoneId));
            final Date parse = tDf.parse(tDateTimeStr);
            return UTC_DATETIME_FORMAT.get().format(parse);
        }catch (Exception e){
            throw new BaseException(BaseStatusEnum.ILLEGAL_ARGUMENT.getCode(), "无法转换成 UTC 时间，格式有误, tDateTimeStr="+ tDateTimeStr + ",zoneId=" + zoneId);
        }
    }

    public static String toUtcDateString(String dateStr , String zoneId){
        try {
            final SimpleDateFormat tDf = DATE_FORMAT.get();
            tDf.setTimeZone(TimeUtil.getTimeZoneByZoneId(zoneId));
            final Date parse = tDf.parse(dateStr);
            return UTC_DATE_FORMAT.get().format(parse);
        }catch (Exception e){
            throw new BaseException(BaseStatusEnum.ILLEGAL_ARGUMENT.getCode(), "无法转换成 UTC 日期，格式有误, dateStr="+ dateStr + ",zoneId=" + zoneId);
        }
    }



    public static TimeZone getTimeZoneByZoneId(String zoneId) {
        val check = checkTimeZoneId(zoneId);
        if (!check) {
            throw new BaseException(BaseStatusEnum.ILLEGAL_ARGUMENT.getCode(), "无效的 Time Zone Id : " + zoneId);
        }
        return TimeZone.getTimeZone(ZoneId.of(zoneId));
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean checkTimeZoneId(String timeZoneId) {
        try {
            ZoneId.of(timeZoneId);
        } catch (Exception e) {
            return false;
        }
        return true;
    }



    /**
     * 截取日期部分
     *
     * @param date 日期
     * @return 日期（时分秒信息会被清空）
     */
    @SneakyThrows
    public static Date truncate(Date date) {
        if (date == null) {
            return null;
        }
        final SimpleDateFormat simpleDateFormat = COMMON_DATE_FORMAT.get();
        return simpleDateFormat.parse(simpleDateFormat.format(date));
    }
    /**
     * 获取距离指定日期 N 天的日期，N 可以为负数
     */
    public static Date obtainNextNDay(Date date,int n){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date == null ? new Date():date);
        calendar.add(Calendar.DATE,n);
        return calendar.getTime();
    }

    public static Date obtainCurrentDate(){
        return truncate(new Date());
    }

    public static String obtainCurrentDateString(){
        return formatDate(new Date());
    }


    public static String formatDate(Date date){
        if (date == null) {
            return null;
        }
        return COMMON_DATE_FORMAT.get().format(date);
    }

    public static Integer getDateNumber(Date date){
        if (date == null) {
            return 0;
        }
        return Integer.parseInt(NUMBER_DATE_FORMAT.get().format(date));
    }

    /**
     * parse，如果输入无效则返回 null
     * @param dateString yyyy-MM-dd
     * @return 日期
     */

    public static Date parseDate(String dateString){
        try {
            return COMMON_DATE_FORMAT.get().parse(dateString);
        }catch (Exception ignore){

        }
        return null;
    }


    public void clear(){
        COMMON_DATE_FORMAT.remove();
        NUMBER_DATE_FORMAT.remove();
    }

}
