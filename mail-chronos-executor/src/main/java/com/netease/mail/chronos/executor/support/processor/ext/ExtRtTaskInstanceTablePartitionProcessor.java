package com.netease.mail.chronos.executor.support.processor.ext;

import com.netease.mail.chronos.executor.support.service.auxiliary.impl.SpExtTaskInstanceHandleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;


/**
 * @author Echo009
 * @since 2021/9/30
 *
 * 分区管理
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtRtTaskInstanceTablePartitionProcessor implements BasicProcessor {


    private final SpExtTaskInstanceHandleServiceImpl spTaskInstanceHandleService;

    @Override
    public ProcessResult process(TaskContext context){
        spTaskInstanceHandleService.updatePartition();
        return new ProcessResult(true,"do nothing");
    }


}
