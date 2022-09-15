package com.netease.mail.chronos.portal.service;

import com.netease.mail.chronos.portal.entity.support.SpExtRemindTaskInfo;
import com.netease.mail.chronos.portal.param.SimpleRemindTask;
import com.netease.mail.chronos.portal.vo.SimpleRemindTaskVo;
import com.netease.mail.master.Device;

import java.util.List;

/**
 * @author Echo009
 * @since 2021/9/21
 */
public interface ExternalSpRemindTaskManageService {

    /**
     * 同步任务
     *
     * @param uid    用户 id
     * @param device 设备信息
     * @return 同步任务
     */
    List<SpExtRemindTaskInfo> syn(String uid, Device device, List<SimpleRemindTask> taskList);

    /**
     *
     * 查询提醒任务列表
     *
     * @param uid    用户 id
     * @param device 设备信息
     * @return 任务列表
     */
    List<SimpleRemindTaskVo> list(String uid, Device device);

}
