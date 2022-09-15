drop table if exists sx_sp_ext_remind_task_info;
create table sx_sp_ext_remind_task_info
(
    id                bigint primary key,
    f_id              varchar(128) not null comment '客户端传递的 ID，用于增量同步处理',
    col_id            varchar(128) not null comment '集合 ID',
    comp_id           varchar(128) not null comment '组件 ID',
    uid               varchar(128) not null comment '用户ID',
    param             longtext comment '任务参数',
    extra             longtext comment '附加信息',
    next_trigger_time bigint       not null comment '触发时间',
    trigger_times     int          not null default 0 comment '触发次数',
    enable            tinyint      not null default 1 comment '是否启用',
    update_time       datetime     not null comment '更新时间',
    create_time       datetime     not null comment '创建时间',
    key idx1_ext_remind_task_info (next_trigger_time, enable),
    key idx2_ext_remind_task_info (uid),
    key idx4_ext_remind_task_info (comp_id, col_id),
    unique key idx3_ext_remind_task_info (f_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='外域提醒任务原始信息';

drop table if exists sx_sp_ext_rt_instance;
CREATE TABLE `sx_sp_ext_rt_instance`
(
    `id`                    bigint       NOT NULL,
    `task_id`               bigint       NOT NULL COMMENT '任务 ID',
    `custom_id`             varchar(128) NOT NULL COMMENT '业务方定义的 ID，用作查询',
    `custom_key`            varchar(128) NOT NULL COMMENT '业务方定义的 key，用作查询',
    `param`                 longtext COLLATE utf8mb4_general_ci COMMENT '任务参数',
    `extra`                 longtext COLLATE utf8mb4_general_ci COMMENT '附加信息（JSON）',
    `expected_trigger_time` bigint       NOT NULL COMMENT '期望触发时间',
    `actual_trigger_time`   bigint                DEFAULT NULL COMMENT '实际触发时间（记录的是首次执行时间）',
    `finished_time`         bigint                DEFAULT NULL COMMENT '完成时间',
    `running_times`         int          NOT NULL DEFAULT '0' COMMENT '运行次数',
    `max_retry_times`       int          NOT NULL DEFAULT '0' COMMENT '最大重试次数,< 0 代表不限',
    `result`                longtext COLLATE utf8mb4_general_ci COMMENT '执行结果(取决于业务逻辑)',
    `status`                int          NOT NULL DEFAULT '0' COMMENT '状态(执行状态)',
    `enable`                tinyint      NOT NULL DEFAULT '1' COMMENT '是否启用，失败且不需要重试，或者手动停止的这个状态会为置为 0 ',
    `partition_key`         int          not null comment '分区键',
    `update_time`           datetime     NOT NULL COMMENT '更新时间',
    `create_time`           datetime     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`, `partition_key`),
    unique INDEX `udx1_ext_rt_instance` (`task_id`, `expected_trigger_time`, `partition_key`),
    KEY `idx1_ext_rt_instance` (`expected_trigger_time`, `enable`, `status`),
    KEY `idx2_ext_rt_instance` (`custom_id`),
    KEY `idx3_ext_rt_instance` (`custom_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='外域提醒任务实例,rt means remind task'
    PARTITION BY RANGE (partition_key)(PARTITION p0 VALUES LESS THAN (20220914) ENGINE = InnoDB,
        PARTITION p20220915 VALUES LESS THAN (20220916) ENGINE = InnoDB,
        PARTITION p20220916 VALUES LESS THAN (20220917) ENGINE = InnoDB,
        PARTITION p20220917 VALUES LESS THAN (20220918) ENGINE = InnoDB,
        PARTITION p20220918 VALUES LESS THAN (20220919) ENGINE = InnoDB);

alter TABLE `sx_sp_ext_rt_instance`
    add PARTITION (
        PARTITION p20220919 VALUES LESS THAN (20220920) ENGINE = InnoDB
        );

select partition_name part, partition_expression expr, partition_description descr, table_rows
from information_schema.partitions
where table_schema = schema()
  and table_name = 'sx_sp_ext_rt_instance';



