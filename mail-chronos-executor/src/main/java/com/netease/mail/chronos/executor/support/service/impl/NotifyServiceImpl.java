package com.netease.mail.chronos.executor.support.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.netease.mail.chronos.executor.support.entity.base.TaskInstance;
import com.netease.mail.chronos.executor.support.service.NotifyService;
import com.netease.mail.mp.api.notify.client.NotifyClient;
import com.netease.mail.mp.api.notify.dto.GenericNotifyRequest;
import com.netease.mail.mp.notify.common.dto.NotifyParamDTO;
import com.netease.mail.quark.status.StatusResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Echo009
 * @since 2021/10/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    private final NotifyClient notifyClient;
    /**
     * 内域，大师号
     */
    private static final int MESSAGE_TYPE_CN = 213;
    /**
     * 内域，大师号
     */
    private static final int MESSAGE_TYPE_EN = 235;
    /**
     * 内域，邮箱
     */
    private static final int MESSAGE_TYPE_MAIL_CN = 398;
    /**
     * 内域，邮箱
     */
    private static final int MESSAGE_TYPE_MAIL_EN = 399;
    /**
     * 外域提醒，
     */
    private static final int EXT_MESSAGE_TYPE_CN = 396;
    /**
     * 外域提醒，大师号
     */
    private static final int EXT_MESSAGE_TYPE_EN = 397;


    @Override
    public boolean sendNotify(TaskInstance taskInstance, boolean external) {
        List<NotifyParamDTO> params = new ArrayList<>();
        HashMap<String, Object> originParams = JSON.parseObject(taskInstance.getParam(), new TypeReference<HashMap<String, Object>>() {
        });
        originParams.forEach((key, value) -> {
            NotifyParamDTO param;
            if (value instanceof String) {
                param = new NotifyParamDTO(key, (String) value);
                param.setJson(false);
            } else {
                param = new NotifyParamDTO(key, JSON.toJSONString(value));
                // 这里不得不这么判断一下，对方用的是 parseObject 方法
                param.setJson(isValidateJsonObjectString(param.getValue()));
            }
            params.add(param);
        });
        // 传递 expectedTriggerTime
        NotifyParamDTO triggerTime = new NotifyParamDTO("expectedTriggerTime", String.valueOf(taskInstance.getExpectedTriggerTime()));
        params.add(triggerTime);

        GenericNotifyRequest.Builder builder = GenericNotifyRequest.newBuilder();
        builder.token(generateToken(taskInstance))
                .params(params)
                .type(chooseMsgType(external,taskInstance));

        // 处理 uid ，这次的原始 uid 有可能是 muid 或者 uid
        if (isRealUid(taskInstance.getCustomKey())) {
            builder.uid(taskInstance.getCustomKey());
        } else {
            builder.muid(taskInstance.getCustomKey());
        }
        StatusResult statusResult = notifyClient.notifyByDomain(builder.build());
        // 记录结果
        taskInstance.setResult(JSON.toJSONString(statusResult));
        if (statusResult.getCode() != 200) {
            log.error("处理提醒任务实例(id:{},compId:{},uid:{})失败,rtn = {}", taskInstance.getId(), taskInstance.getCustomId(), taskInstance.getCustomKey(), statusResult);
            return false;
        }
        log.info("处理提醒任务实例(id:{},compId:{},uid:{})成功,rtn = {}", taskInstance.getId(), taskInstance.getCustomId(), taskInstance.getCustomKey(), statusResult);
        return true;
    }

    private int chooseMsgType(boolean external, TaskInstance taskInstance) {
        // 判断是否使用英文模板
        boolean useEn = false;
        String extra = taskInstance.getExtra();
        if (extra != null) {
            try {
                Map<String, String> map = JSON.parseObject(extra, new TypeReference<Map<String, String>>() {
                });
                String locale = map.get("locale");
                Locale toLocale = LocaleUtils.toLocale(locale);
                if (toLocale != null && Locale.ENGLISH.getLanguage().equals(toLocale.getLanguage())) {
                    useEn = true;
                }
            } catch (Exception e) {
                log.error("解析任务语言失败，使用默认语言：中文", e);
            }

        }
        if (external){
            return useEn ? EXT_MESSAGE_TYPE_EN : EXT_MESSAGE_TYPE_CN;
        }
        if (isRealUid(taskInstance.getCustomKey())){
            return useEn ? MESSAGE_TYPE_MAIL_EN : MESSAGE_TYPE_MAIL_CN;
        }else {
            return useEn ? MESSAGE_TYPE_EN : MESSAGE_TYPE_CN;
        }

    }

    private boolean isRealUid(String uid) {
        return StringUtils.isNotBlank(uid) && uid.contains("@");
    }

    private boolean isValidateJsonObjectString(String value) {
        try {
            JSON.parseObject(value);
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    private String generateToken(TaskInstance taskInstance) {
        // 根据 id 和 triggerTime 生成唯一 id
        Long id = taskInstance.getId();
        Long nextTriggerTime = taskInstance.getExpectedTriggerTime();
        return id + "#" + nextTriggerTime;
    }
}
