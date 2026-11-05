package com.example.sharding.controller;

import com.example.sharding.common.ApiResponse;
import com.example.sharding.entity.AppConfig;
import com.example.sharding.service.AppConfigService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final AppConfigService appConfigService;

    @Data
    public static class ConfigUpsertRequest {
        @NotBlank(message = "value 不能为空")
        private String value;

        private String remark;
    }

    @PostMapping("/{key}")
    public ApiResponse<AppConfig> save(@PathVariable String key, @RequestBody @jakarta.validation.Valid ConfigUpsertRequest request) {
        return ApiResponse.ok(appConfigService.save(key, request.getValue(), request.getRemark()));
    }

    @GetMapping("/{key}")
    public ApiResponse<AppConfig> get(@PathVariable String key) {
        return ApiResponse.ok(appConfigService.get(key));
    }
}
