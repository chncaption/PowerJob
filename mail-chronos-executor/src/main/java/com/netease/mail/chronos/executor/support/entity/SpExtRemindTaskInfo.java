package com.netease.mail.chronos.executor.support.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 提醒任务信息
 * @author  sp_ext_remind_task_info
 */
@TableName(value ="sp_ext_remind_task_info")
@Data
@Accessors(chain = true)
public class SpExtRemindTaskInfo implements Serializable {
    /**
     *
     */
    @TableId
    private Long id;
    /**
     * 客户端定义的 id
     */
    private String fId;

    private String colId;
    /**
     * 组件 ID
     *
     * 注意: compId 需保证唯一
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
     * 下次触发时间
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
     * 被禁用的时间
     */
    private Date disableTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}