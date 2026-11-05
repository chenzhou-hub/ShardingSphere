package com.example.sharding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sharding.common.ApiResponse;
import com.example.sharding.entity.Order;
import com.example.sharding.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Data
    public static class OrderCreateRequest {
        @NotNull(message = "userId 不能为空")
        private Long userId;

        @NotNull(message = "amount 不能为空")
        @DecimalMin(value = "0.01", message = "amount 必须大于 0")
        private BigDecimal amount;

        @NotBlank(message = "status 不能为空")
        private String status;
    }

    @PostMapping
    public ApiResponse<Order> create(@RequestBody @Valid OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request.getUserId(), request.getAmount(), request.getStatus()));
    }

    @GetMapping
    public ApiResponse<Page<Order>> page(@RequestParam long userId,
                                         @RequestParam(defaultValue = "1") long pageNo,
                                         @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(orderService.pageByUser(userId, pageNo, pageSize));
    }

    @GetMapping("/{userId}/{orderId}")
    public ApiResponse<Order> detail(@PathVariable long userId, @PathVariable long orderId) {
        return ApiResponse.ok(orderService.getByUserAndId(userId, orderId));
    }

    @GetMapping("/count")
    public ApiResponse<Long> count() {
        return ApiResponse.ok(orderService.countAll());
    }
}
