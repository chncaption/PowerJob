package com.netease.mail.chronos.executor.support.service;

import com.netease.mail.chronos.executor.support.entity.base.TaskInstance;

/**
 * @author Echo009
 * @since 2021/10/21
 */
public interface NotifyService {


    /**
     * 发送通知
     * @param spRtTaskInstance 提醒任务
     * @param external 是否是外域
     */
    boolean sendNotify(TaskInstance spRtTaskInstance,boolean external);


}
