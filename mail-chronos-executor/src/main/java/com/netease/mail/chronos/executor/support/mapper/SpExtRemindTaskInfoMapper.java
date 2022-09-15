package com.netease.mail.chronos.executor.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.netease.mail.chronos.executor.support.entity.SpExtRemindTaskInfo;
import com.netease.mail.chronos.executor.support.po.SpRemindTaskSimpleInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ...
 */
public interface SpExtRemindTaskInfoMapper extends BaseMapper<SpExtRemindTaskInfo> {

    /**
     * 获取 nextTriggerTime 小于等于指定时间的任务 ID
     * @param maxTriggerTime 触发时间阈值
     * @return ID List (仅含 trigger time)
     */
    List<SpRemindTaskSimpleInfo>  selectIdListByNextTriggerTimeAndEnable(@Param("maxTriggerTime")long maxTriggerTime);

    List<SpRemindTaskSimpleInfo>  selectIdListByNextTriggerTimeAndEnableLimit(@Param("maxTriggerTime")long maxTriggerTime, @Param("limit") int limit);

}




