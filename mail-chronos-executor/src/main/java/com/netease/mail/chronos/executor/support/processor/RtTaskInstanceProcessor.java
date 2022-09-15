package com.netease.mail.chronos.executor.support.processor;

import com.netease.mail.chronos.executor.support.base.po.TaskInstancePrimaryKey;
import com.netease.mail.chronos.executor.support.entity.SpRtTaskInstance;
import com.netease.mail.chronos.executor.support.service.NotifyService;
import com.netease.mail.chronos.executor.support.service.auxiliary.impl.SpTaskInstanceHandleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;



/**
 * @author Echo009
 * @since 2021/10/29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RtTaskInstanceProcessor extends AbstractTaskInstanceMapProcessor<SpRtTaskInstance> {


    private final NotifyService notifyService;


    private final SpTaskInstanceHandleServiceImpl spTaskInstanceHandleService;


    @Override
    public String obtainTaskDesc() {
        return "内域提醒";
    }

    @Override
    public List<TaskInstancePrimaryKey> loadNeedHandleInstanceIdList(int maxSize) {
        return spTaskInstanceHandleService.loadHandleInstanceIdList(maxSize);
    }

    @Override
    public void updateTaskInstance(SpRtTaskInstance taskInstance) {
        spTaskInstanceHandleService.updateByPrimaryKey(taskInstance);
    }

    @Override
    public List<SpRtTaskInstance> loadTaskInstanceList(List<TaskInstancePrimaryKey> taskInstancePrimaryKeyList) {
        return spTaskInstanceHandleService.selectByPrimaryKeyList(taskInstancePrimaryKeyList);
    }

    @Override
    public boolean processCore(SpRtTaskInstance taskInstance) {
        return notifyService.sendNotify(taskInstance,false);
    }
}
