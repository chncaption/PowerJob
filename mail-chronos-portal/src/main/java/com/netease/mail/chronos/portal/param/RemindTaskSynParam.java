package com.netease.mail.chronos.portal.param;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Echo009
 * @since 2022/9/14
 */
@Data
@Accessors(chain = true)
public class RemindTaskSynParam {

    private Long t;

    private String sign;

    private String uid;

    private List<SimpleRemindTask> taskList;


}
