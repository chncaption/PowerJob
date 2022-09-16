package com.netease.mail.chronos.portal.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author Echo009
 * @since 2022/9/14
 */
@Data
@Accessors(chain = true)
public class SimpleRemindTask {
    /**
     * id
     */
    private String id;
    /**
     * 集合 ID
     */
    private String calId;
    /**
     * 组件 ID
     */
    private String compId;
    /**
     * 引用
     */
    private String href;
    /**
     * 标题
     */
    private String title;
    /**
     * 提醒内容
     */
    private String content;
    /**
     * 期望触发时间
     */
    private Long expectTriggerTime;
    /**
     * 预留字段
     */
    private Map<String,Object> extra;


}
