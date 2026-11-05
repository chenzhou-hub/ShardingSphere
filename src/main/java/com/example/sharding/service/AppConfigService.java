package com.example.sharding.service;

import com.example.sharding.entity.AppConfig;
import com.example.sharding.mapper.AppConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigMapper appConfigMapper;

    /** 读取配置（单表 t_config 固定在 ds0，不分片） */
    public AppConfig get(String key) {
        return appConfigMapper.selectById(key);
    }

    /** 保存或更新配置（upsert） */
    @Transactional
    public AppConfig save(String key, String value, String remark) {
        AppConfig config = appConfigMapper.selectById(key);
        if (config == null) {
            config = new AppConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setRemark(remark);
            config.setUpdateTime(LocalDateTime.now());
            appConfigMapper.insert(config);
        } else {
            config.setConfigValue(value);
            if (remark != null) {
                config.setRemark(remark);
            }
            config.setUpdateTime(LocalDateTime.now());
            appConfigMapper.updateById(config);
        }
        return config;
    }
}
