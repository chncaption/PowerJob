package com.netease.mail.chronos.portal.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.netease.mail.chronos.base.exception.BaseException;
import com.netease.mail.chronos.portal.entity.support.SpExtRemindTaskInfo;
import com.netease.mail.chronos.portal.enums.ExternalTaskBaseStatusEnum;
import com.netease.mail.chronos.portal.mapper.support.SpExtRemindTaskInfoMapper;
import com.netease.mail.chronos.portal.param.SimpleRemindTask;
import com.netease.mail.chronos.portal.service.ExternalSpRemindTaskManageService;
import com.netease.mail.chronos.portal.util.QueryWrapperUtil;
import com.netease.mail.chronos.portal.vo.SimpleRemindTaskVo;
import com.netease.mail.master.Device;
import com.netease.mail.quark.commons.log.TimeCostLog;
import com.netease.mail.quark.commons.serialization.JacksonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Echo009
 * @since 2022/9/15
 */
@Service
@Slf4j
public class ExternalSpRemindTaskManageServiceImpl implements ExternalSpRemindTaskManageService {

    @Value("${master.external.remindTask.maxNum:50}")
    private Integer maxExternalRemindTaskNum;

    private final SpExtRemindTaskInfoMapper spExtRemindTaskInfoMapper;

    private final Snowflake snowflake;

    private static final String UID_COL_NAME = "uid";

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ExternalSpRemindTaskManageServiceImpl(SpExtRemindTaskInfoMapper spExtRemindTaskInfoMapper, @Qualifier("exRemindTaskIdGenerator") Snowflake snowflake) {
        this.spExtRemindTaskInfoMapper = spExtRemindTaskInfoMapper;
        this.snowflake = snowflake;
    }


    @Override
    @Transactional(value = "chronosSupportTransactionManager", rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public List<SpExtRemindTaskInfo> syn(String uid, Device device, List<SimpleRemindTask> taskList) {
        TimeCostLog timeCostLog = new TimeCostLog("cmd", "syn");
        timeCostLog.put("uid", uid);
        timeCostLog.put("device", device);
        timeCostLog.put("taskList", taskList);

        if (taskList!= null && taskList.size() > maxExternalRemindTaskNum){
            timeCostLog.put("msg","too many task");
            log.warn("{}", timeCostLog);
            throw new BaseException(ExternalTaskBaseStatusEnum.TOO_MANY_TASK);
        }
        try {
            final List<SpExtRemindTaskInfo> originList = spExtRemindTaskInfoMapper.selectList(QueryWrapperUtil.construct(UID_COL_NAME, uid));
            timeCostLog.put("originTaskList", originList);
            // clean
            if (CollectionUtils.isEmpty(taskList)) {
                if (!originList.isEmpty()) {
                    timeCostLog.put("msg", "only clear all task");
                    spExtRemindTaskInfoMapper.deleteBatchIds(originList.stream().map(SpExtRemindTaskInfo::getId).collect(Collectors.toList()));
                }
                log.info("{}", timeCostLog);
                return Collections.emptyList();
            }
            if (!originList.isEmpty()){
                // 直接 delete
                spExtRemindTaskInfoMapper.deleteBatchIds(originList.stream().map(SpExtRemindTaskInfo::getId).collect(Collectors.toList()));
            }
            // 再 insert
            List<SpExtRemindTaskInfo> rtnList = new ArrayList<>();
            for (SimpleRemindTask simpleRemindTask : taskList) {
                final SpExtRemindTaskInfo insert = constructBySimpleRemindTask(uid, simpleRemindTask);
                spExtRemindTaskInfoMapper.insert(insert);
                rtnList.add(insert);
            }
            log.info("{}", timeCostLog);
            return rtnList;
        } catch (Exception e) {
            log.error("{}", timeCostLog, e);
            // 对外屏蔽错误信息
            throw new BaseException(ExternalTaskBaseStatusEnum.UNKNOWN);
        }
    }


    @Override
    public List<SimpleRemindTaskVo> list(String uid,Device device) {
        TimeCostLog timeCostLog = new TimeCostLog("cmd", "list");
        timeCostLog.put("uid", uid);
        timeCostLog.put("device", device);
        try {
            List<SpExtRemindTaskInfo> originList = spExtRemindTaskInfoMapper.selectList(QueryWrapperUtil.construct(UID_COL_NAME, uid));
            final List<SimpleRemindTaskVo> rtnList = originList.stream().map(this::constructBySpExtRemindTaskInfo).collect(Collectors.toList());
            timeCostLog.put("result", rtnList);
            log.info("{}",timeCostLog);
            return rtnList;
        }catch (Exception e){
            log.error("{}", timeCostLog, e);
            // 对外屏蔽错误信息
            throw new BaseException(ExternalTaskBaseStatusEnum.UNKNOWN);
        }
    }


    private SimpleRemindTaskVo constructBySpExtRemindTaskInfo(SpExtRemindTaskInfo input){
        final SimpleRemindTaskVo rtn = new SimpleRemindTaskVo();

        rtn.setId(input.getFId());
        rtn.setColId(input.getColId());
        rtn.setCompId(input.getCompId());

        final Params params = JacksonUtils.deserialize(input.getParam(), Params.class);
        rtn.setHref(params.href);
        rtn.setTitle(params.title);
        rtn.setContent(params.getContent());

        rtn.setExpectTriggerTime(input.getNextTriggerTime());
        // 预留
        rtn.setExtra(new HashMap<>(8));
        return rtn;
    }


    private SpExtRemindTaskInfo constructBySimpleRemindTask(String uid, SimpleRemindTask input) {
        final SpExtRemindTaskInfo spExtRemindTaskInfo = new SpExtRemindTaskInfo();
        spExtRemindTaskInfo.setId(snowflake.nextId());
        spExtRemindTaskInfo.setUid(uid);
        spExtRemindTaskInfo.setFId(input.getId());
        spExtRemindTaskInfo.setColId(input.getColId());
        spExtRemindTaskInfo.setCompId(input.getCompId());

        final Params params = new Params();
        params.setColId(input.getColId());
        params.setCompId(input.getCompId());
        params.setHref(input.getHref());
        params.setTitle(input.getTitle());
        params.setContent(StringUtils.isBlank(input.getContent()) ? "" : input.getContent());
        spExtRemindTaskInfo.setParam(JacksonUtils.toString(params));

        // extra 预留

        spExtRemindTaskInfo.setNextTriggerTime(input.getExpectTriggerTime());
        spExtRemindTaskInfo.setTriggerTimes(0);
        spExtRemindTaskInfo.setEnable(input.getExpectTriggerTime() > System.currentTimeMillis());
        final Date now = new Date();
        if (!spExtRemindTaskInfo.getEnable()) {
            spExtRemindTaskInfo.setDisableTime(now);
        }
        spExtRemindTaskInfo.setCreateTime(now);
        spExtRemindTaskInfo.setUpdateTime(now);

        return spExtRemindTaskInfo;
    }

    @Data
    public static class Params {

        private String colId;

        private String compId;

        private String href;

        private String title;

        private String content;

    }
}
