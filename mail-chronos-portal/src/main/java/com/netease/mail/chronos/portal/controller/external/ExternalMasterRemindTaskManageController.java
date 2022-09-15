package com.netease.mail.chronos.portal.controller.external;

import com.netease.mail.chronos.base.exception.BaseException;
import com.netease.mail.chronos.base.response.BaseResponse;
import com.netease.mail.chronos.portal.config.AuthConfig;
import com.netease.mail.chronos.portal.entity.support.SpExtRemindTaskInfo;
import com.netease.mail.chronos.portal.enums.ExternalTaskBaseStatusEnum;
import com.netease.mail.chronos.portal.param.RemindTaskSynParam;
import com.netease.mail.chronos.portal.service.ExternalSpRemindTaskManageService;
import com.netease.mail.chronos.portal.util.SignUtil;
import com.netease.mail.chronos.portal.vo.SimpleRemindTaskVo;
import com.netease.mail.master.Device;
import com.netease.mail.master.MasterfpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Echo009
 * @since 2022/9/14
 *
 * 外域待办任务管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/external/master/remind_task")
@Slf4j
public class ExternalMasterRemindTaskManageController {


    private final AuthConfig authConfig;

    private final ExternalSpRemindTaskManageService externalSpRemindTaskManageService;

    /**
     * 同步外域待办任务
     * https://gzags.mail.netease.com/#/projects/127/api-specs/2651
     */
    @PostMapping("/syn")
    public BaseResponse<List<SpExtRemindTaskInfo>> syn(
            @RequestHeader(name = "masterfp",required = false) String masterfp,
            @RequestBody RemindTaskSynParam taskSynParam,
            @RequestParam(required = false, defaultValue = "false") boolean disableMasterFpCheck,
            @RequestParam(required = false, defaultValue = "false") boolean disableTimestampCheck,
            @RequestParam(required = false, defaultValue = "false") boolean disableSignCheck
    ) {
        if (taskSynParam == null || taskSynParam.getUid() == null ){
            throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ARGUMENT);
        }
        Device device = null;
        if (!authConfig.isAllowDisableMasterFpCheck() || !disableMasterFpCheck){
            device = MasterfpUtils.getInstance().getMasterDevice(masterfp);
            if (device == null) {
                log.warn("[cmd:syn,invalid masterfp,param:{},masterfp:{}]", taskSynParam, masterfp);
                throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ACCESS.getCode(), "invalid masterfp!");
            }
        }
        // check params
        if (!authConfig.isAllowDisableTimestampCheck() || !disableTimestampCheck) {
            if (!checkT(taskSynParam.getT())) {
                log.warn("[cmd:syn,out of date request,param:{},device:{}]", taskSynParam, device);
                throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ACCESS.getCode(), "out of date request!");
            }
        }
        if (!authConfig.isAllowDisableSignCheck() || !disableSignCheck) {
            // check sign
            final String sign = SignUtil.genSign(Arrays.asList(String.valueOf(taskSynParam.getT()), taskSynParam.getUid(), taskSynParam.getTaskList() == null ? "0" : String.valueOf(taskSynParam.getTaskList().size())));
            if (!StringUtils.equalsIgnoreCase(sign, taskSynParam.getSign())) {
                log.warn("[cmd:syn,invalid sign,param:{},device:{},expected sign:{}]", taskSynParam, device, sign);
                throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ACCESS.getCode(), "invalid sign!");
            }
        }
        return BaseResponse.success(externalSpRemindTaskManageService.syn(taskSynParam.getUid(), device, taskSynParam.getTaskList()));
    }


    /**
     * 查询待办任务列表
     * https://gzags.mail.netease.com/#/projects/127/api-specs/2652
     */
    @GetMapping("/list")
    public BaseResponse<List<SimpleRemindTaskVo>> list(
            @RequestHeader(name = "masterfp",required = false) String masterfp,
            @RequestParam Long t,
            @RequestParam String sign,
            @RequestParam String uid,
            @RequestParam(required = false, defaultValue = "false") boolean disableMasterFpCheck,
            @RequestParam(required = false, defaultValue = "false") boolean disableTimestampCheck,
            @RequestParam(required = false, defaultValue = "false") boolean disableSignCheck
    ) {
        Device device = null;
        if (!authConfig.isAllowDisableMasterFpCheck() || !disableMasterFpCheck){
            device = MasterfpUtils.getInstance().getMasterDevice(masterfp);
            if (device == null) {
                log.warn("[cmd:list,invalid masterfp,uid:{},masterfp:{}]", uid, masterfp);
                throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ACCESS.getCode(), "invalid masterfp!");
            }
        }
        if (!authConfig.isAllowDisableTimestampCheck() || !disableTimestampCheck) {
            if (!checkT(t)) {
                log.warn("[cmd:list,out of date request,uid:{},device:{}]", uid, device);
                throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ACCESS.getCode(), "out of date request!");
            }
        }
        if (!authConfig.isAllowDisableSignCheck() || !disableSignCheck) {
            // check sign
            final String expectedSign = SignUtil.genSign(Arrays.asList(String.valueOf(t), uid));
            if (!StringUtils.equalsIgnoreCase(sign, expectedSign)) {
                log.warn("[cmd:list,invalid sign,uid:{},device:{},sign:{},expected sign:{}]", uid, device, sign, expectedSign);
                throw new BaseException(ExternalTaskBaseStatusEnum.ILLEGAL_ACCESS.getCode(), "invalid sign!");
            }
        }
        return BaseResponse.success(externalSpRemindTaskManageService.list(uid, device));
    }


    private boolean checkT(Long t) {
        // 检查时间戳,防止重放
        long timestamp = t == null ? 0 : t;
        return System.currentTimeMillis() - timestamp <= authConfig.getMaxInterval();
    }



}
