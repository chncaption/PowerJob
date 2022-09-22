package com.netease.mail.chronos.executor.support.processor;

import cn.hutool.core.lang.Holder;
import cn.hutool.core.lang.Snowflake;
import com.fasterxml.jackson.core.type.TypeReference;
import com.netease.mail.chronos.base.utils.ExecuteUtil;
import com.netease.mail.chronos.base.utils.ICalendarRecurrenceRuleUtil;
import com.netease.mail.chronos.base.utils.TimeUtil;
import com.netease.mail.chronos.executor.support.entity.SpRemindTaskInfo;
import com.netease.mail.chronos.executor.support.entity.SpRtTaskInstance;
import com.netease.mail.chronos.executor.support.enums.RtTaskInstanceStatus;
import com.netease.mail.chronos.executor.support.service.SpRemindTaskService;
import com.netease.mail.chronos.executor.support.service.auxiliary.impl.SpTaskInstanceHandleServiceImpl;
import com.netease.mail.quark.commons.serialization.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Property;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author Echo009
 * @since 2021/9/24
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RemindTaskProcessor extends AbstractTaskMapProcessor<SpRemindTaskInfo> {

    private final Snowflake snowflake = new Snowflake();

    private final SpRemindTaskService spRemindTaskService;

    private final SpTaskInstanceHandleServiceImpl spTaskInstanceHandleService;

    private static final String DT_START = "dtStart";

    private static final String DT_END = "dtEnd";

    private static final String DT_START_TZ_ID = "dtStartTzId";

    @Override
    public String obtainDesc() {
        return "内域提醒";
    }

    @Override
    protected List<Long> loadValidTaskIdList(Long maxTriggerTime, int maxSize) {
        return spRemindTaskService.obtainValidTaskIdListByTriggerTimeThreshold(maxTriggerTime, maxSize);
    }

    @Override
    protected SpRemindTaskInfo loadTaskById(Long id) {
        return spRemindTaskService.selectById(id);
    }

    @Override
    protected boolean shouldSkip(long maxTriggerTime, SpRemindTaskInfo task) {
        if (task == null) {
            return true;
        }
        if (task.getEnable() != null && !task.getEnable()) {
            log.warn("提醒任务(id:{},colId:{},compId:{}) 已经被禁用，跳过处理", task.getId(), task.getColId(), task.getCompId());
            return true;
        }
        // 检查 nextTriggerTime 是否已经变更（重试需要保证幂等）
        if (task.getNextTriggerTime() == null
                || task.getNextTriggerTime() >= maxTriggerTime) {
            log.warn("提醒任务(id:{},colId:{},compId:{})本次调度已被成功处理过，跳过", task.getId(), task.getColId(), task.getCompId());
            return true;
        }
        return false;
    }

    @Override
    protected void processCore(SpRemindTaskInfo task) {
        // 生成实例入库
        SpRtTaskInstance construct = construct(task);
        Holder<Boolean> exceptionHolder = new Holder<>(false);
        // 这里会保证幂等性
        ExecuteUtil.executeIgnoreSpecifiedExceptionWithoutReturn(() -> spTaskInstanceHandleService.insert(construct), DuplicateKeyException.class, exceptionHolder);
        // 记录重复了则说明这条记录已经处理过，无需更新触发次数
        if (Boolean.FALSE.equals(exceptionHolder.get())) {
            // 更新状态
            task.setTriggerTimes(task.getTriggerTimes() + 1);
            log.info("更新任务({})触发次数 {} => {},本次期望触发时间为 {}", task.getId(), task.getTriggerTimes() - 1, task.getTriggerTimes(), task.getNextTriggerTime());

            // INTERVAL 之前的任务 现在才触发，打印日志，表示这个任务延迟太严重，正常情况下不应该出现
            if (task.getNextTriggerTime() < System.currentTimeMillis() - INTERVAL) {
                log.warn("当前任务处理延迟过高(> {} ms),task detail:({})", INTERVAL, task);
            }
        }
        // 计算下次调度时间 , 理论上不应该会存在每分钟调度一次的提醒任务（业务场景决定）
        String recurrenceRule = task.getRecurrenceRule();
        // 为空直接 disable (触发一次的任务)
        if (StringUtils.isBlank(recurrenceRule)) {
            disableTask(task);
        } else {
            updateTriggerTime(task);
        }
        task.setUpdateTime(new Date());
        spRemindTaskService.updateById(task);
    }

    private SpRtTaskInstance construct(SpRemindTaskInfo task) {

        SpRtTaskInstance spRtTaskInstance = new SpRtTaskInstance();
        // 基础信息
        spRtTaskInstance.setTaskId(task.getId());
        spRtTaskInstance.setCustomId(task.getCompId());
        spRtTaskInstance.setCustomKey(task.getUid());
        // 这里需要特殊处理一下参数，计算距离第一次触发的偏移值
        final String paramStr = task.getParam();
        try {
            Map<String, Object> paramMap = JacksonUtils.deserialize(paramStr, new TypeReference<Map<String, Object>>() {
            });
            // 计算偏移值
            long firstTriggerTime = task.getStartTime() + task.getTriggerOffset();
            long currentOffset = task.getNextTriggerTime() - firstTriggerTime;
            paramMap.put("offset", currentOffset);
            // 兼容旧版本任务数据，后续可以去掉
            if (paramMap.get(DT_START) == null && paramMap.get(DT_END) == null){
                paramMap.put(DT_START, 0);
                paramMap.put(DT_END, 0);
            }
            paramMap.computeIfAbsent(DT_START_TZ_ID, k -> task.getTimeZoneId());
            spRtTaskInstance.setParam(JacksonUtils.toString(paramMap));
        } catch (Exception e) {
            log.warn("处理任务偏移值失败,task detail:({})", task);
            spRtTaskInstance.setParam(task.getParam());
        }
        spRtTaskInstance.setExtra(task.getExtra());
        // 运行信息
        spRtTaskInstance.setRunningTimes(0);
        // 最多重试 6 次
        spRtTaskInstance.setMaxRetryTimes(6);
        spRtTaskInstance.setExpectedTriggerTime(task.getNextTriggerTime());
        spRtTaskInstance.setEnable(true);
        spRtTaskInstance.setStatus(RtTaskInstanceStatus.INIT.getCode());
        // 其他
        spRtTaskInstance.setCreateTime(new Date());
        spRtTaskInstance.setUpdateTime(new Date());
        spRtTaskInstance.setPartitionKey(TimeUtil.getDateNumber(new Date()));
        spRtTaskInstance.setId(snowflake.nextId());

        return spRtTaskInstance;

    }


    public void disableTask(SpRemindTaskInfo spRemindTaskInfo) {
        spRemindTaskInfo.setEnable(false);
        spRemindTaskInfo.setDisableTime(new Date());
    }



    public void updateTriggerTime(SpRemindTaskInfo spRemindTaskInfo) {
        try {
            // support EXDATE
            final List<String> exDateList = parseExDateList(spRemindTaskInfo);

            long time = System.currentTimeMillis();
            // 参考时间取 nextTriggerTime 和 当前时间较大的值
            if (spRemindTaskInfo.getNextTriggerTime() != null) {
                time = Math.max(System.currentTimeMillis(), spRemindTaskInfo.getNextTriggerTime());
            }
            // 更新 nextTriggerTime , 不处理 miss fire 的情形 （从业务场景上来说，没有必要）
            long nextTriggerTime = ICalendarRecurrenceRuleUtil.calculateNextTriggerTimeExDateListWithOffset(spRemindTaskInfo.getRecurrenceRule(), spRemindTaskInfo.getStartTime(), spRemindTaskInfo.getTriggerOffset(), time, exDateList, spRemindTaskInfo.getTimeZoneId());
            // 检查生命周期
            handleLifeCycle(spRemindTaskInfo, nextTriggerTime);
        } catch (Exception e) {
            // 记录异常信息
            log.error("处理任务(id:{},colId:{},compId:{})失败，计算下次触发时间失败，已将其自动禁用，请检查重复规则表达式是否合法！recurrenceRule:{}", spRemindTaskInfo.getId(), spRemindTaskInfo.getColId(), spRemindTaskInfo.getCompId(), spRemindTaskInfo.getRecurrenceRule(), e);
            disableTask(spRemindTaskInfo);
        }
    }



    private  void handleLifeCycle(SpRemindTaskInfo spRemindTaskInfo, long nextTriggerTime) {
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



    public static List<String> parseExDateList(SpRemindTaskInfo spRemindTaskInfo) {
        if (StringUtils.isBlank(spRemindTaskInfo.getExtra())) {
            return Collections.emptyList();
        }
        try {
            final Map<String, Object> map = JacksonUtils.deserialize(spRemindTaskInfo.getExtra(), new TypeReference<Map<String, Object>>() {
            });
            final Object v = map.get(Property.EXDATE);
            if (v == null) {
                return Collections.emptyList();
            }
            return JacksonUtils.deserialize(JacksonUtils.toString(v), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("[cmd:parseExDateList,msg:failed,extra:{}]", spRemindTaskInfo.getExtra(), e);
            // ignore
            return Collections.emptyList();
        }
    }
}
