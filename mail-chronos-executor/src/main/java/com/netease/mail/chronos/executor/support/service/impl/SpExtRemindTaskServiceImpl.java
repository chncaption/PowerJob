package com.netease.mail.chronos.executor.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netease.mail.chronos.base.utils.TimeUtil;
import com.netease.mail.chronos.executor.support.entity.SpExtRemindTaskInfo;
import com.netease.mail.chronos.executor.support.mapper.SpExtRemindTaskInfoMapper;
import com.netease.mail.chronos.executor.support.po.SpRemindTaskSimpleInfo;
import com.netease.mail.chronos.executor.support.service.SpExtRemindTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Echo009
 * @since 2022/9/15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SpExtRemindTaskServiceImpl implements SpExtRemindTaskService {

    private final SpExtRemindTaskInfoMapper spExtRemindTaskInfoMapper;


    @Override
    public List<Long> obtainValidTaskIdListByTriggerTimeThreshold(long maxTriggerTime, int limit) {
        // 拉取小于 maxTriggerTime 的任务
        List<SpRemindTaskSimpleInfo> spRemindTaskInfos = spExtRemindTaskInfoMapper.selectIdListByNextTriggerTimeAndEnableLimit(maxTriggerTime,limit);
        // 过滤小于 minTriggerTime
        if (spRemindTaskInfos == null || spRemindTaskInfos.isEmpty()) {
            return Collections.emptyList();
        }
        return spRemindTaskInfos.stream()
                .map(SpRemindTaskSimpleInfo::getId)
                .collect(Collectors.toList());
    }

    @Override
    public SpExtRemindTaskInfo selectById(long id) {
        return spExtRemindTaskInfoMapper.selectById(id);
    }

    @Override
    public int updateById(SpExtRemindTaskInfo spExtRemindTaskInfo) {
        return spExtRemindTaskInfoMapper.updateById(spExtRemindTaskInfo);
    }

    @Override
    public List<SpExtRemindTaskInfo> obtainOutOfDateDisableTask() {
        Date b15 = TimeUtil.obtainNextNDay(new Date(), -15);
        QueryWrapper<SpExtRemindTaskInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("enable", false).le("disable_time", b15);
        return spExtRemindTaskInfoMapper.selectList(wrapper);
    }

    @Override
    public List<SpExtRemindTaskInfo> obtainStagnantTask() {
        // 超过 5 分钟没有触发的，且未被 disable
        long threshold = System.currentTimeMillis() - 300_000L;
        QueryWrapper<SpExtRemindTaskInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("enable", true).le("next_trigger_time", threshold);
        return spExtRemindTaskInfoMapper.selectList(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        spExtRemindTaskInfoMapper.deleteById(id);
    }
}
