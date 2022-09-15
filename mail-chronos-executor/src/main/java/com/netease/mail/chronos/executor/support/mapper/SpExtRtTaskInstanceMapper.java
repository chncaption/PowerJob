package com.netease.mail.chronos.executor.support.mapper;

import com.netease.mail.chronos.executor.support.base.mapper.TaskInstanceBaseMapper;
import com.netease.mail.chronos.executor.support.base.po.TaskInstancePrimaryKey;
import com.netease.mail.chronos.executor.support.entity.SpExtRtTaskInstance;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ...
 */
public interface SpExtRtTaskInstanceMapper extends TaskInstanceBaseMapper<SpExtRtTaskInstance> {
    /**
     * 加载需要被处理的任务
     * triggerTime < threshold
     * enable = 1
     * status ！= 3
     */
    @Override
    List<TaskInstancePrimaryKey> selectIdListOfNeedTriggerInstance(@Param("threshold") Long threshold, @Param("partitionKeyList") List<Integer> partitionKeyList, @Param("limit") int limit);

    @Override
    SpExtRtTaskInstance selectByPrimaryKey(@Param("id") Long id, @Param("partitionKey") Integer partitionKey);


    @Override
    List<SpExtRtTaskInstance> selectByIdListAndPartitionKeyList(@Param("idList") List<Long> idList, @Param("partitionKeyList") List<Integer> partitionKeyList);

    @Override
    int updateByPrimaryKey(@Param("taskInstance") SpExtRtTaskInstance taskInstance);

    /**
     * ! 这里会忽略唯一索引重复的异常
     */
    @Override
    int insert(@Param("taskInstance") SpExtRtTaskInstance taskInstance);

    @Override
    void createPartition(@Param("partitionName") String partitionName, @Param("valueLimit") Integer valueLimit);

    @Override
    void dropPartition(@Param("partitionName") String partitionName);
}




