package com.netease.mail.chronos.executor.support.service;

import com.netease.mail.chronos.executor.support.entity.SpRtTaskInstance;

/**
 * @author Echo009
 * @since 2021/10/21
 */
public interface NotifyService {


    /**
     * 发送通知
     * @param spRtTaskInstance 提醒任务
     */
    boolean sendNotify(SpRtTaskInstance spRtTaskInstance);


}
