# 分库初始化脚本：ds0 -> order_db_0（含 t_order_0~3 与单表 t_config）

CREATE TABLE IF NOT EXISTS t_order_0 (
    order_id    BIGINT        NOT NULL COMMENT '订单ID(雪花算法)',
    user_id     BIGINT        NOT NULL COMMENT '用户ID(分库键)',
    amount      DECIMAL(12,2) NOT NULL COMMENT '订单金额',
    status      VARCHAR(32)   NOT NULL COMMENT '订单状态',
    create_time DATETIME      NOT NULL COMMENT '创建时间',
    update_time DATETIME      NULL COMMENT '更新时间',
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表-分片0';

CREATE TABLE IF NOT EXISTS t_order_1 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表-分片1';

CREATE TABLE IF NOT EXISTS t_order_2 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表-分片2';

CREATE TABLE IF NOT EXISTS t_order_3 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表-分片3';

CREATE TABLE IF NOT EXISTS t_config (
    config_key   VARCHAR(64)  NOT NULL COMMENT '配置键',
    config_value VARCHAR(512) NOT NULL COMMENT '配置值',
    remark       VARCHAR(255) NULL COMMENT '备注',
    update_time  DATETIME     NULL COMMENT '更新时间',
    PRIMARY KEY (config_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '系统配置表(不分片)';
