package com.netease.mail.chronos.executor.support.processor;

import com.netease.mail.chronos.executor.support.entity.SpRemindTaskInfo;
import com.netease.mail.chronos.executor.support.service.SpRemindTaskService;
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
public class OutOfDateTaskProcessor extends AbstractOutOfDateProcessor<SpRemindTaskInfo> {

    private final SpRemindTaskService spRemindTaskService;

    @Override
    protected void deleteTask(SpRemindTaskInfo task) {
        spRemindTaskService.deleteById(task.getId());
    }

    @Override
    protected List<SpRemindTaskInfo> obtainOutOfDateDisableTask() {
        return spRemindTaskService.obtainOutOfDateDisableTask();
    }
}
