package com.example.sharding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置表（单表：固定落库 ds0.t_config，不分片）
 */
@Data
@TableName("t_config")
public class AppConfig {

    @TableId(value = "config_key", type = IdType.INPUT)
    private String configKey;

    private String configValue;

    private String remark;

    private LocalDateTime updateTime;
}
