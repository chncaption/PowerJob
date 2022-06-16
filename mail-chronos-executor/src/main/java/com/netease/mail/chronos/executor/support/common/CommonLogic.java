package com.netease.mail.chronos.executor.support.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netease.mail.chronos.base.utils.ICalendarRecurrenceRuleUtil;
import com.netease.mail.chronos.executor.support.entity.SpRemindTaskInfo;
import com.netease.mail.chronos.executor.support.entity.SpRtTaskInstance;
import com.netease.mail.quark.commons.serialization.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Property;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Echo009
 * @since 2021/9/30
 */
@Slf4j
public class CommonLogic {


    public static void updateTriggerTime(SpRemindTaskInfo spRemindTaskInfo) {
        try {
            // support EXDATE
            final List<String> exDateList = parseExDateList(spRemindTaskInfo.getExtra());
            // 更新 nextTriggerTime , 不处理 miss fire 的情形 （从业务场景上来说，没有必要）
            long nextTriggerTime = ICalendarRecurrenceRuleUtil.calculateNextTriggerTime(spRemindTaskInfo.getRecurrenceRule(), spRemindTaskInfo.getStartTime() + spRemindTaskInfo.getTriggerOffset(), System.currentTimeMillis(), exDateList);
            // 检查生命周期
            handleLifeCycle(spRemindTaskInfo, nextTriggerTime);
        } catch (Exception e) {
            // 记录异常信息
            log.error("处理任务(id:{},colId:{},compId:{})失败，计算下次触发时间失败，已将其自动禁用，请检查重复规则表达式是否合法！recurrenceRule:{}", spRemindTaskInfo.getId(), spRemindTaskInfo.getColId(), spRemindTaskInfo.getCompId(), spRemindTaskInfo.getRecurrenceRule(), e);
            disableTask(spRemindTaskInfo);
        }
    }

    private static void handleLifeCycle(SpRemindTaskInfo spRemindTaskInfo, long nextTriggerTime) {
        // 当不存在下一次调度时间时，nextTriggerTime = 0
        if (nextTriggerTime == 0L) {
            disableTask(spRemindTaskInfo);
        } else if (spRemindTaskInfo.getEndTime() != null && spRemindTaskInfo.getEndTime() < nextTriggerTime) {
            disableTask(spRemindTaskInfo);
        } else if (spRemindTaskInfo.getTimesLimit() > 0 && spRemindTaskInfo.getTriggerTimes() >= spRemindTaskInfo.getTimesLimit()) {
            disableTask(spRemindTaskInfo);
        } else {
            spRemindTaskInfo.setNextTriggerTime(nextTriggerTime);
        }
    }


    public static void disableTask(SpRemindTaskInfo spRemindTaskInfo) {
        spRemindTaskInfo.setEnable(false);
        spRemindTaskInfo.setDisableTime(new Date());
    }


    public static void disableInstance(SpRtTaskInstance spRtTaskInstance) {
        spRtTaskInstance.setEnable(false);
        spRtTaskInstance.setUpdateTime(new Date());
    }


    public static List<String> parseExDateList(String extra) {
        if (StringUtils.isBlank(extra)) {
            return Collections.emptyList();
        }
        try {
            final Map<String, Object> map = JacksonUtils.deserialize(extra, new TypeReference<Map<String, Object>>() {
            });
            final Object v = map.get(Property.EXDATE);
            if (v == null) {
                return Collections.emptyList();
            }
            return JacksonUtils.deserialize(JacksonUtils.toString(v), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("[cmd:parseExDateList,msg:failed,extra:{}]", extra, e);
            // ignore
            return Collections.emptyList();
        }
    }


}
