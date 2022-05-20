package com.netease.mail.chronos.executor.fulfill.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.netease.mail.chronos.base.exception.BaseException;
import com.netease.mail.chronos.context.common.enums.AccountTypeEnum;
import com.netease.mail.chronos.executor.fulfill.util.VerifyUtil;
import com.netease.mail.quark.status.StatusResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cylee.commons.httpclient.HttpClientUtil;
import org.cylee.commons.serializer.JacksonUtil;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import java.util.Map;

import static com.netease.mail.chronos.executor.constant.HttpConstant.HTTP_SUCCESS_CODE;

/**
 * @author Echo009
 * @since 2022/5/20
 * <p>
 * 简单的履约 api 调用（只传递账户信息），处理没法生成订单的场景下的履约，如邮箱会员绑定大师权益包
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SimpleFulfillApiProcessor implements BasicProcessor {

    private final HttpClientUtil httpClientUtil;

    @Override
    @SneakyThrows
    public ProcessResult process(TaskContext context) {
        SimpleParam simpleParam = parseParam(context);
        // send request
        ImmutableMap.Builder<String, String> paramsBuider = ImmutableMap.<String, String>builder()
                .put("uid", simpleParam.account)
                .put("privilegeId", simpleParam.getPrvlgId())
                .put("fulfillId", String.valueOf(context.getInstanceId()))
                .putAll(VerifyUtil.getVerifyParamsBySalt(simpleParam.getSalt()));

        ImmutableMap<String, String> params = paramsBuider.build();

        String res = httpClientUtil.post(simpleParam.address, params, "UTF-8", "application/x-www-form-urlencoded; charset=UTF-8");
        context.getOmsLogger().info("params: {}", params);
        context.getOmsLogger().info("response: {}", res);
        StatusResult<Map<String, Object>> statusResult = JacksonUtil.toObj(res,
                new TypeReference<StatusResult<Map<String, Object>>>() {
                });
        if (statusResult.getCode() != HTTP_SUCCESS_CODE) {
            return new ProcessResult(false, "ERROR" + res);
        }
        Integer status = (Integer) statusResult.getResult().get("status");
        // 履约成功会返回 100 ，注意目前是不支持异步处理的
        if (status == null || status != 100) {
            return new ProcessResult(false, "FAILED" + res);
        }
        return new ProcessResult(true, "" + res);
    }

    /**
     * 解析参数
     */
    private SimpleParam parseParam(TaskContext context) {
        SimpleParam res = new SimpleParam();
        try {
            JobParam jobParam = JSON.parseObject(context.getJobParams(), JobParam.class);
            if (StringUtils.isBlank(jobParam.getAddress()) || StringUtils.isBlank(jobParam.getSalt()) || StringUtils.isBlank(jobParam.getAccountType())) {
                context.getOmsLogger().error("任务静态参数非法! JobParam:{}", context.getJobParams());
                throw new BaseException("任务静态参数非法!");
            }
            res.setAddress(jobParam.address);
            res.setSalt(jobParam.salt);
            res.setPrvlgId(jobParam.getPrvlgId());
            AccountTypeEnum accountTypeEnum = AccountTypeEnum.getByCodeOrDefault(jobParam.getAccountType());
            // 提取 account
            if (context.getWorkflowContext().getWfInstanceId() != null) {
                // 从工作流中读取
                Map<String, String> data = context.getWorkflowContext().getData();
                String account = data.get(accountTypeEnum.getKey());
                if (StringUtils.isBlank(account)) {
                    context.getOmsLogger().error("无法从工作流上下文中提取账户信息! context:{}", context.getWorkflowContext().getData());
                    throw new BaseException("任务静态参数非法!");
                }
                res.setAccount(account);

            } else {
                // 从任务实例参数中获取
                JSONObject jsonObject = JSON.parseObject(context.getInstanceParams());
                String account = jsonObject.getString("account");
                if (StringUtils.isBlank(account)) {
                    context.getOmsLogger().error("无法从实例参数中提取账户信息! context:{}", context.getWorkflowContext().getData());
                    throw new BaseException("任务静态参数非法!");
                }
                res.setAccount(account);
            }
            return res;
        } catch (Exception e) {
            if (e instanceof BaseException) {
                throw (BaseException) e;
            }
            context.getOmsLogger().error("解析任务静态参数失败! JobParam:{}", context.getJobParams(), e);
            throw new BaseException("解析任务静态参数失败!");
        }
    }

    @Data
    public static class JobParam {

        private String salt;

        private String address;

        private String accountType;

        private String prvlgId;

    }


    @Data
    public static class SimpleParam {
        /**
         * 从上下文或者从实例参数中取
         */
        private String account;

        private String salt;

        private String prvlgId;

        private String address;


    }


}
