package com.netease.mail.chronos.executor.support.service.auxiliary.impl;

import com.netease.mail.chronos.executor.support.base.mapper.TaskInstanceBaseMapper;
import com.netease.mail.chronos.executor.support.entity.SpExtRtTaskInstance;
import com.netease.mail.chronos.executor.support.enums.TaskInstanceHandleStrategy;
import com.netease.mail.chronos.executor.support.mapper.SpExtRtTaskInstanceMapper;
import com.netease.mail.chronos.executor.support.service.auxiliary.AbstractTaskInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author Echo009
 * @since 2022/09/15
 *
 * 提醒任务实例
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SpExtTaskInstanceHandleServiceImpl extends AbstractTaskInstanceService<SpExtRtTaskInstance> {

    private final SpExtRtTaskInstanceMapper spExtRtTaskInstanceMapper;

    @Override
    public long getThresholdDelta() {
        // 10 s
        return 10000;
    }

    @Override
    public int getScope() {
        return 2;
    }

    @Override
    public TaskInstanceHandleStrategy matchStrategy() {
        return TaskInstanceHandleStrategy.EXT_REMIND_TASK;
    }

    @Override
    public TaskInstanceBaseMapper<SpExtRtTaskInstance> getMapper() {
        return spExtRtTaskInstanceMapper;
    }
}
