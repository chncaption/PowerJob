package com.netease.mail.chronos.executor.support.service;

import com.netease.mail.chronos.executor.support.entity.SpExtRemindTaskInfo;
import com.netease.mail.chronos.executor.support.entity.SpRemindTaskInfo;

import java.util.List;

/**
 * @author Echo009
 * @since 2022/9/15
 */
public interface SpExtRemindTaskService {


    /**
     * 加载触发时间小于指定时间的任务 id 列表
     * @param maxTriggerTime 最大触发时间
     * @param limit 最大数量
     * @return id list
     */
    List<Long> obtainValidTaskIdListByTriggerTimeThreshold(long maxTriggerTime,int limit);

    /**
     * 根据 ID 查找记录
     * @param id 任务 id
     * @return 提醒任务详情
     */
    SpExtRemindTaskInfo selectById(long id);

    /**
     * 根据 ID 更新记录
     * @param spExtRemindTaskInfo 提醒任务详情
     * @return 更新记录数
     */
    int updateById(SpExtRemindTaskInfo spExtRemindTaskInfo);

    /**
     * 获取需要被清理的任务
     */
    List<SpExtRemindTaskInfo> obtainOutOfDateDisableTask();

    /**
     * 获取停滞的任务
     * trigger time 已过期，且没有被 disable
     */
    List<SpExtRemindTaskInfo> obtainStagnantTask();


    void deleteById(Long id);

}
