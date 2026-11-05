-- H2 测试库 ds1 初始化
CREATE TABLE IF NOT EXISTS t_order_0 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id)
);
CREATE TABLE IF NOT EXISTS t_order_1 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id)
);
CREATE TABLE IF NOT EXISTS t_order_2 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id)
);
CREATE TABLE IF NOT EXISTS t_order_3 (
    order_id    BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    status      VARCHAR(32)   NOT NULL,
    create_time DATETIME      NOT NULL,
    update_time DATETIME      NULL,
    PRIMARY KEY (order_id)
);
