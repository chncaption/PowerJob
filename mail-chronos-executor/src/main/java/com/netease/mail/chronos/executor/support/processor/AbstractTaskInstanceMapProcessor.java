package com.netease.mail.chronos.executor.support.processor;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.netease.mail.chronos.base.exception.BaseException;
import com.netease.mail.chronos.base.utils.ExceptionUtil;
import com.netease.mail.chronos.base.utils.ExecuteUtil;
import com.netease.mail.chronos.executor.support.base.po.TaskInstancePrimaryKey;
import com.netease.mail.chronos.executor.support.common.TaskSplitParam;
import com.netease.mail.chronos.executor.support.entity.base.TaskInstance;
import com.netease.mail.chronos.executor.support.enums.RtTaskInstanceStatus;
import com.netease.mail.uaInfo.UaInfoContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.MapProcessor;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Echo009
 * @since 2022/9/15
 *
 * 集成默认的数据加载以及任务分片的处理逻辑
 */
@Slf4j
public abstract class AbstractTaskInstanceMapProcessor<T extends TaskInstance> implements MapProcessor {


    protected static final HashMap<String, Object> FAKE_UA = Maps.newHashMap();
    /**
     * 每一批处理的数量
     */
    protected static final Integer BATCH_SIZE = 100;
    /**
     * 最大处理数量
     */
    protected static final Integer MAX_SIZE = 50_0000;

    static {
        FAKE_UA.put("fakeUa", "ignore");
        FAKE_UA.put("source", "chronos");
    }

    /**
     * 获取任务描述，用于日志
     *
     * @return 任务描述
     */
    public abstract String obtainTaskDesc();

    /**
     * 加载需要处理的任务实例
     *
     * @param maxSize 最大数量
     * @return 任务实例列表
     */
    public abstract List<TaskInstancePrimaryKey> loadNeedHandleInstanceIdList(int maxSize);

    /**
     * 更新任务实例
     *
     * @param taskInstance 任务实例
     */
    public abstract void updateTaskInstance(T taskInstance);

    /**
     * 加载任务实例列表
     *
     * @param taskInstancePrimaryKeyList 任务实例ID列表
     * @return 任务实例列表
     */
    public abstract List<T> loadTaskInstanceList(List<TaskInstancePrimaryKey> taskInstancePrimaryKeyList);

    /**
     * 处理任务实例
     *
     * @param taskInstance 任务实例
     * @return 是否处理成功
     */
    public abstract boolean processCore(T taskInstance);

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        TaskSplitParam taskSplitParam = TaskSplitParam.parseOrDefault(context.getJobParams(), BATCH_SIZE, MAX_SIZE);
        if (isRootTask()) {
            List<TaskInstancePrimaryKey> taskInstancePrimaryKeys = loadNeedHandleInstanceIdList(taskSplitParam.getMaxSize());
            if (taskInstancePrimaryKeys == null || taskInstancePrimaryKeys.isEmpty()) {
                log.info("本次没有需要处理的{}任务实例! ", obtainTaskDesc());
                return new ProcessResult(true, "本次没有需要处理的" + obtainTaskDesc() + "任务实例");
            }
            // 小于阈值直接执行
            if (taskInstancePrimaryKeys.size() <= taskSplitParam.getBatchSize()) {
                log.info("本次无需进行任务分片! 一共 {} 条", taskInstancePrimaryKeys.size());
                processList(0, taskInstancePrimaryKeys);
                return new ProcessResult(true, "任务不需要分片,处理成功!");
            }
            log.info("开始切分任务! batchSize:{}", taskSplitParam.getBatchSize());
            List<RtTaskInstanceProcessor.SubTask> subTaskList = new LinkedList<>();
            // 切割任务
            List<List<TaskInstancePrimaryKey>> idListList = CollUtil.split(taskInstancePrimaryKeys, taskSplitParam.getBatchSize());
            int count = 0;
            for (List<TaskInstancePrimaryKey> list : idListList) {
                // start from 1
                RtTaskInstanceProcessor.SubTask subTask = new RtTaskInstanceProcessor.SubTask(++count, list);
                subTaskList.add(subTask);
            }
            map(subTaskList, "ProcessRemindTaskInstance-" + obtainTaskDesc());
            return new ProcessResult(true, "切分任务成功,total:" + subTaskList.size());

        } else {
            RtTaskInstanceProcessor.SubTask subTask = (RtTaskInstanceProcessor.SubTask) context.getSubTask();
            log.info("开始处理任务分片 {},size:{}", subTask.getSeq(), subTask.getIdList().size());
            List<TaskInstancePrimaryKey> idList = subTask.getIdList();
            processList(subTask.getSeq(), idList);
            log.info("处理任务分片({})成功,size:{}", subTask.getSeq(), subTask.getIdList().size());
            return new ProcessResult(true, "处理任务分片(" + subTask.getSeq() + ")成功!");
        }
    }

    @SuppressWarnings("squid:S3776")
    protected void processList(int sliceSeq, List<TaskInstancePrimaryKey> idList) {
        UaInfoContext.setUaInfo(FAKE_UA);
        int errorCount = 0;
        final List<T> taskInstanceList = loadTaskInstanceList(idList);
        for (T taskInstance : taskInstanceList) {
            if (Thread.interrupted()){
                // 被中断
                log.warn("当前任务已经被主动中断！");
                throw new BaseException("任务被主动中断!");
            }
            try {
                // 判断是否需要跳过
                if (shouldSkip(taskInstance)) {
                    // 更新状态为禁用
                    if (taskInstance != null) {
                        taskInstance.setEnable(false);
                        taskInstance.setUpdateTime(new Date());
                        updateTaskInstance(taskInstance);
                    }
                    continue;
                }
                // 记录首次触发时间
                if (taskInstance.getActualTriggerTime() == null) {
                    taskInstance.setActualTriggerTime(System.currentTimeMillis());
                }
                // 处理
                boolean res = processCore(taskInstance);
                // 记录完成时间
                taskInstance.setFinishedTime(System.currentTimeMillis());
                // 更新状态
                taskInstance.setStatus(res ? RtTaskInstanceStatus.SUCCESS.getCode() : RtTaskInstanceStatus.FAILED.getCode());
            } catch (Exception e) {
                // 数据库异常 或者 网络异常
                log.error("处理任务({})失败 ！", taskInstance, e);
                errorCount++;
                if (taskInstance != null) {
                    taskInstance.setResult(ExceptionUtil.getExceptionDesc(e));
                    taskInstance.setStatus(RtTaskInstanceStatus.FAILED.getCode());
                }
            }
            if (taskInstance != null) {
                // 更新运行次数
                taskInstance.setRunningTimes(taskInstance.getRunningTimes() + 1);
                // 如果这里失败了，本轮不重试，等下一轮调度
                ExecuteUtil.executeIgnoreExceptionWithoutReturn(() -> updateTaskInstance(taskInstance), "update remind task instance:" + taskInstance.getId());
            }
        }
        if (errorCount != 0) {
            log.info("处理任务分片({})失败,total:{},failure:{}", sliceSeq, idList.size(), errorCount);
            throw new BaseException("任务分片处理失败,seq:" + sliceSeq);
        }
    }


    private boolean shouldSkip(T taskInstance) {
        if (taskInstance == null) {
            return true;
        }
        if (taskInstance.getStatus() != null && taskInstance.getStatus().equals(RtTaskInstanceStatus.SUCCESS.getCode())) {
            log.warn("任务实例(id:{},compId:{},expectedTriggerTime:{}) 已执行成功，跳过处理", taskInstance.getId(), taskInstance.getCustomId(), taskInstance.getExpectedTriggerTime());
            return true;
        }
        if (taskInstance.getEnable() != null && !taskInstance.getEnable()) {
            log.warn("任务实例(id:{},compId:{},expectedTriggerTime:{}) 已经被禁用，跳过处理", taskInstance.getId(), taskInstance.getCustomId(), taskInstance.getExpectedTriggerTime());
            return true;
        }
        // 检查是否已经超过最大重试次数
        if (taskInstance.getRunningTimes() != null
                && taskInstance.getMaxRetryTimes() != null
                && taskInstance.getRunningTimes() > taskInstance.getMaxRetryTimes()) {
            log.warn("任务实例(id:{},compId:{},expectedTriggerTime:{})已超过最大运行次数", taskInstance.getId(), taskInstance.getCustomId(), taskInstance.getExpectedTriggerTime());
            return true;
        }
        return false;
    }


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubTask {

        private int seq;

        private List<TaskInstancePrimaryKey> idList;

    }


}
