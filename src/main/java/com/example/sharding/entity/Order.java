package com.example.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表（分片表：ds{0..1}.t_order_{0..3}）
 * order_id 由 ShardingSphere SNOWFLAKE 分布式主键生成，应用侧无需赋值。
 */
@Data
@TableName("t_order")
public class Order {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /** 分库键 */
    private Long userId;

    private BigDecimal amount;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
