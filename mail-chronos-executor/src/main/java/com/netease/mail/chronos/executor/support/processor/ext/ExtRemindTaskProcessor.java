package com.netease.mail.chronos.executor.support.processor.ext;

import cn.hutool.core.lang.Holder;
import cn.hutool.core.lang.Snowflake;
import com.netease.mail.chronos.base.utils.ExecuteUtil;
import com.netease.mail.chronos.base.utils.TimeUtil;
import com.netease.mail.chronos.executor.support.entity.SpExtRemindTaskInfo;
import com.netease.mail.chronos.executor.support.entity.SpExtRtTaskInstance;
import com.netease.mail.chronos.executor.support.enums.RtTaskInstanceStatus;
import com.netease.mail.chronos.executor.support.processor.AbstractTaskMapProcessor;
import com.netease.mail.chronos.executor.support.service.SpExtRemindTaskService;
import com.netease.mail.chronos.executor.support.service.auxiliary.impl.SpExtTaskInstanceHandleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author Echo009
 * @since 2022/9/15
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtRemindTaskProcessor extends AbstractTaskMapProcessor<SpExtRemindTaskInfo> {

    private final Snowflake snowflake = new Snowflake();

    private final SpExtRemindTaskService spExtRemindTaskService;

    private final SpExtTaskInstanceHandleServiceImpl spTaskInstanceHandleService;

    @Override
    public String obtainDesc() {
        return "外域提醒";
    }

    @Override
    protected List<Long> loadValidTaskIdList(Long maxTriggerTime, int maxSize) {
        return spExtRemindTaskService.obtainValidTaskIdListByTriggerTimeThreshold(maxTriggerTime, maxSize);
    }

    @Override
    protected SpExtRemindTaskInfo loadTaskById(Long id) {
        return spExtRemindTaskService.selectById(id);
    }

    @Override
    protected boolean shouldSkip(long maxTriggerTime, SpExtRemindTaskInfo task) {
        if (task == null) {
            return true;
        }
        if (task.getNextTriggerTime() == null
                || task.getNextTriggerTime() >= maxTriggerTime) {
            log.warn("提醒任务(id:{},colId:{},compId:{})本次调度已被成功处理过，跳过", task.getId(), task.getColId(), task.getCompId());
            return true;
        }
        if (task.getEnable() != null && !task.getEnable()) {
            log.warn("提醒任务(id:{},colId:{},compId:{}) 已经被禁用，跳过处理", task.getId(), task.getColId(), task.getCompId());
            return true;
        }
        return false;
    }

    @Override
    protected void processCore(SpExtRemindTaskInfo task) {

        // 生成实例入库
        SpExtRtTaskInstance construct = construct(task);
        Holder<Boolean> exceptionHolder = new Holder<>(false);
        // 这里会保证幂等性
        ExecuteUtil.executeIgnoreSpecifiedExceptionWithoutReturn(() -> spTaskInstanceHandleService.insert(construct), DuplicateKeyException.class, exceptionHolder);
        // 记录重复了则说明这条记录已经处理过，无需更新触发次数
        if (Boolean.FALSE.equals(exceptionHolder.get())) {
            // INTERVAL 之前的任务 现在才触发，打印日志，表示这个任务延迟太严重，正常情况下不应该出现
            if (task.getNextTriggerTime() < System.currentTimeMillis() - INTERVAL) {
                log.warn("当前任务处理延迟过高(> {} ms),task detail:({})", INTERVAL, task);
            }
            // 更新状态
            task.setTriggerTimes(task.getTriggerTimes() + 1);
            log.info("更新任务({})触发次数 {} => {},本次期望触发时间为 {}", task.getId(), task.getTriggerTimes() - 1, task.getTriggerTimes(), task.getNextTriggerTime());
        }
        task.setEnable(false);
        task.setDisableTime(new Date());
        task.setUpdateTime(new Date());
        spExtRemindTaskService.updateById(task);
    }

    private SpExtRtTaskInstance construct(SpExtRemindTaskInfo task) {

        SpExtRtTaskInstance spRtTaskInstance = new SpExtRtTaskInstance();
        // 基础信息
        spRtTaskInstance.setTaskId(task.getId());
        spRtTaskInstance.setCustomId(task.getCompId());
        spRtTaskInstance.setCustomKey(task.getUid());
        spRtTaskInstance.setParam(task.getParam());
        spRtTaskInstance.setExtra(task.getExtra());
        // 最多重试 6 次
        spRtTaskInstance.setMaxRetryTimes(6);
        spRtTaskInstance.setExpectedTriggerTime(task.getNextTriggerTime());
        spRtTaskInstance.setEnable(true);
        spRtTaskInstance.setStatus(RtTaskInstanceStatus.INIT.getCode());
        // 运行信息
        spRtTaskInstance.setRunningTimes(0);
        // 其他
        spRtTaskInstance.setCreateTime(new Date());
        spRtTaskInstance.setUpdateTime(new Date());
        spRtTaskInstance.setPartitionKey(TimeUtil.getDateNumber(new Date()));
        spRtTaskInstance.setId(snowflake.nextId());

        return spRtTaskInstance;

    }


}
