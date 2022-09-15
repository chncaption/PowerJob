package com.netease.mail.chronos.executor.support.processor.ext;

import com.netease.mail.chronos.executor.support.base.po.TaskInstancePrimaryKey;
import com.netease.mail.chronos.executor.support.entity.SpExtRtTaskInstance;
import com.netease.mail.chronos.executor.support.processor.AbstractTaskInstanceMapProcessor;
import com.netease.mail.chronos.executor.support.service.NotifyService;
import com.netease.mail.chronos.executor.support.service.auxiliary.impl.SpExtTaskInstanceHandleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Echo009
 * @since 2022/09/15
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExtRtTaskInstanceProcessor extends AbstractTaskInstanceMapProcessor<SpExtRtTaskInstance> {


    private final NotifyService notifyService;


    private final SpExtTaskInstanceHandleServiceImpl spExtTaskInstanceHandleService;


    @Override
    public String obtainTaskDesc() {
        return "外域提醒";
    }

    @Override
    public List<TaskInstancePrimaryKey> loadNeedHandleInstanceIdList(int maxSize) {
        return spExtTaskInstanceHandleService.loadHandleInstanceIdList(maxSize);
    }

    @Override
    public void updateTaskInstance(SpExtRtTaskInstance taskInstance) {
        spExtTaskInstanceHandleService.updateByPrimaryKey(taskInstance);
    }

    @Override
    public List<SpExtRtTaskInstance> loadTaskInstanceList(List<TaskInstancePrimaryKey> taskInstancePrimaryKeyList) {
        return spExtTaskInstanceHandleService.selectByPrimaryKeyList(taskInstancePrimaryKeyList);
    }

    @Override
    public boolean processCore(SpExtRtTaskInstance taskInstance) {
        return notifyService.sendNotify(taskInstance,true);
    }
}
