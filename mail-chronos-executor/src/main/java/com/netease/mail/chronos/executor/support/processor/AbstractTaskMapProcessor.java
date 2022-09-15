package com.netease.mail.chronos.executor.support.processor;

import cn.hutool.core.collection.CollUtil;
import com.netease.mail.chronos.base.exception.BaseException;
import com.netease.mail.chronos.executor.support.common.TaskSplitParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.MapProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Echo009
 * @since 2022/9/15
 */
public abstract class AbstractTaskMapProcessor <T> implements MapProcessor {


    /**
     * 每一批处理的数量
     */
    protected static final Integer BATCH_SIZE = 100;
    /**
     * 最大处理数量
     */
    protected static final Integer MAX_SIZE = 50_0000;
    /**
     * 间隙
     */
    protected static final Long INTERVAL = 30_000L;

    /**
     * 获取描述信息，用于日志
     */
    protected abstract String obtainDesc();


    /**
     * 加载任务 id 列表
     */
    protected abstract List<Long> loadValidTaskIdList(Long maxTriggerTime, int maxSize);

    protected abstract T loadTaskById(Long id);

    protected abstract boolean shouldSkip(long maxTriggerTime, T task);

    protected abstract void processCore(T task);


    /**
     * 根据触发时间获取 状态为有效，且下次触发时间小于当前时间 或 未来 30 s 内即将触发的任务
     * 注意，这也意味着重复触发的任务最小间隔为 30 s
     * 处理后后计算下次触发时间，更新状态
     */
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        TaskSplitParam taskSplitParam = TaskSplitParam.parseOrDefault(context.getJobParams(), BATCH_SIZE, MAX_SIZE);
        long maxTriggerTime = System.currentTimeMillis() + INTERVAL;
        if (isRootTask()) {
            // （-,maxTriggerTime),limit
            List<Long> idList = loadValidTaskIdList(maxTriggerTime, taskSplitParam.getMaxSize());
            if (idList == null || idList.isEmpty()) {
                log.info("本次没有需要触发的{}任务! maxTriggerTime:{} , limit:{}",obtainDesc(), maxTriggerTime, taskSplitParam.getMaxSize());
                return new ProcessResult(true, "本次没有需要触发的任务");
            }
            // 小于阈值直接执行
            if (idList.size() <= taskSplitParam.getBatchSize()) {
                log.info("本次无需进行任务分片! 一共 {} 条", idList.size());
                processList(0, idList, maxTriggerTime);
                return new ProcessResult(true, "任务不需要分片,处理成功!");
            }
            log.info("开始切分任务! batchSize:{}", BATCH_SIZE);
            List<AbstractTaskMapProcessor.SubTask> subTaskList = new LinkedList<>();
            // 切割任务
            List<List<Long>> idListList = CollUtil.split(idList, BATCH_SIZE);
            int count = 0;
            for (List<Long> list : idListList) {
                // start from 1
                AbstractTaskMapProcessor.SubTask subTask = new AbstractTaskMapProcessor.SubTask(++count, list);
                subTaskList.add(subTask);
            }
            map(subTaskList, "ProcessRemindTask-"+obtainDesc());
            return new ProcessResult(true, "切分任务成功,total:" + subTaskList.size());

        } else {
            AbstractTaskMapProcessor.SubTask subTask = (AbstractTaskMapProcessor.SubTask) context.getSubTask();
            log.info("开始处理任务分片 {},size:{}", subTask.getSeq(), subTask.getIdList().size());
            List<Long> idList = subTask.getIdList();
            processList(subTask.getSeq(), idList, maxTriggerTime);
            log.info("处理任务分片({})成功,size:{}", subTask.getSeq(), subTask.getIdList().size());
            return new ProcessResult(true, "处理任务分片(" + subTask.getSeq() + ")成功!");
        }
    }


    protected void processList(int sliceSeq, List<Long> idList, long maxTriggerTime) {
        int errorCount = 0;
        for (Long id : idList) {
            try {
                T task = loadTaskById(id);
                // 判断是否需要跳过
                if (shouldSkip(maxTriggerTime, task)) {
                    continue;
                }
                processCore(task);
            } catch (Exception e) {
                log.error("处理任务(id:{})失败 ！", id, e);
                errorCount++;
            }
        }
        if (errorCount != 0) {
            log.info("处理任务分片({})失败,total:{},failure:{}", sliceSeq, idList.size(), errorCount);
            throw new BaseException("任务分片处理失败,seq:" + sliceSeq);
        }
    }



    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubTask {

        private int seq;

        private List<Long> idList;

    }


}
