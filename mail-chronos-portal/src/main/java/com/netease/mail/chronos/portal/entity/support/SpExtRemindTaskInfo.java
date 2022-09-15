package com.netease.mail.chronos.portal.entity.support;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 外域提醒任务原始信息
 * @author  sp_ext_remind_task_info
 */
@TableName(value ="sp_ext_remind_task_info")
@Data
@Accessors(chain = true)
public class SpExtRemindTaskInfo implements Serializable {
    /**
     * 
     */
    private Long id;

    /**
     * 客户端传递的 ID，用于增量同步处理
     */
    private String fId;

    /**
     * 集合 ID
     */
    private String colId;

    /**
     * 组件 ID
     */
    private String compId;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 任务参数
     */
    private String param;

    /**
     * 附加信息
     */
    private String extra;

    /**
     * 触发时间
     */
    private Long nextTriggerTime;

    /**
     * 触发次数
     */
    private Integer triggerTimes;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;

}