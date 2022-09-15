package com.netease.mail.chronos.executor.support.processor.ext;

import com.netease.mail.chronos.executor.support.entity.SpExtRemindTaskInfo;
import com.netease.mail.chronos.executor.support.processor.AbstractOutOfDateProcessor;
import com.netease.mail.chronos.executor.support.service.SpExtRemindTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Echo009
 * @since 2021/9/30
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExtOutOfDateProcessor extends AbstractOutOfDateProcessor<SpExtRemindTaskInfo> {

    private final SpExtRemindTaskService spRemindTaskService;

    @Override
    protected void deleteTask(SpExtRemindTaskInfo task) {
        spRemindTaskService.deleteById(task.getId());
    }

    @Override
    protected List<SpExtRemindTaskInfo> obtainOutOfDateDisableTask() {
        return spRemindTaskService.obtainOutOfDateDisableTask();
    }
}