#表
// {"trace_id":["wx077v2testb23nk"],"ad_id":["2394499769"],"adgroup_id":["2400000070"],"wechat_unionid":["om8_q5rbT3a9J26auXH9sT257P-E"],"click_time":["1606022101"],"add_channel":["H5"],"qywx_corp_id":["wwad7cc050af02e211"],"campaign_id":["2400000068"]}
CREATE TABLE `gdt_wechat_tracking_data`
(
    id              bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    trace_id        varchar(64) not null default '' comment '广告trace_id',
    wechat_union_id varchar(64) not null default '' comment '微信union_id',
    ad_group_id     varchar(64) not null default '' comment '广告组id',
    click_time      varchar(64) not null default '' comment '点击时间',
    qywx_corp_id    varchar(64) not null default '' comment '企业微信主体信息',
    add_channel     varchar(64) not null default '' comment '归因类型',
    campaign_id     varchar(64) not null default '' comment '推广计划ID',
    ad_id           varchar(64) not null default '' comment '广告ID',
    qywx_user_id    varchar(64) not null default '' comment '客服id',
    ad_way          varchar(64) not null default '' comment '添加渠道',
    advertiser_id   varchar(64) not null default '' comment '广告主id',
    canvas_id       varchar(64) not null default '' comment '微信广告原生页id',
    create_by       varchar(16)          default '' not null comment '创建人',
    update_by       varchar(16)          default '' not null comment '更新人',
    create_time     timestamp(3)         default CURRENT_TIMESTAMP(3) not null comment '创建时间',
    update_time     timestamp(3)         default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP (3) comment '最后修改时间',
    PRIMARY KEY (`id`) USING BTREE,
) ENGINE = InnoDB  CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '广点通企微数据';

CREATE TABLE `user_marketing_data`
(
    id              bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    wechat_union_id varchar(64)                               not null default '' comment '微信union_id',
    user_id         varchar(64)                               not null default '' comment 'C用户id',
    register_time   datetime(64) not null default '' comment '注册时间',
    create_by       varchar(16)  default ''                   not null comment '创建人',
    update_by       varchar(16)  default ''                   not null comment '更新人',
    create_time     timestamp(3) default CURRENT_TIMESTAMP(3) not null comment '创建时间',
    update_time     timestamp(3) default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP (3) comment '最后修改时间',
    PRIMARY KEY (`id`) USING BTREE,
) ENGINE = InnoDB  CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '投放用户数据';

CREATE TABLE `user_marketing_event`
(
    id              bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    wechat_union_id varchar(64) not null default '' comment '微信union_id',
    user_id         varchar(64) not null default '' comment 'C用户id',
    event_type      varchar(64) not null default '' comment '事件类型 加微 加群 注册',
    event_detail    varchar(64) not null default '' comment '事件详情',
    market_type     varchar(64) not null default '' comment '投放类型',
    channel         varchar(64) not null default '' comment '投放渠道',
    channel_pattern varchar(64) not null default '' comment '投放形式',
    channel_app     varchar(64) not null default '' comment '投放应用',
    tracking_id     varchar(64) not null default '' comment '投放链路id',
    status          varchar(16)          default '' not null comment '状态',
    create_by       varchar(16)          default '' not null comment '创建人',
    update_by       varchar(16)          default '' not null comment '更新人',
    create_time     timestamp(3)         default CURRENT_TIMESTAMP(3) not null comment '创建时间',
    update_time     timestamp(3)         default CURRENT_TIMESTAMP(3) not null on update CURRENT_TIMESTAMP (3) comment '最后修改时间',
    PRIMARY KEY (`id`) USING BTREE,
) ENGINE = InnoDB  CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '投放用户事件数据';


