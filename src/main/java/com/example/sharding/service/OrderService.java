package com.example.sharding.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.sharding.entity.Order;
import com.example.sharding.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    /**
     * 创建订单。
     * 分片路由：库 = user_id % 2，表 = order_id % 4（order_id 由 ShardingSphere 雪花算法生成）。
     */
    @Transactional
    public Order create(Long userId, java.math.BigDecimal amount, String status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(amount);
        order.setStatus(status);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    /**
     * 按 user_id 精确路由到单库分页查询。
     */
    public Page<Order> pageByUser(long userId, long pageNo, long pageSize) {
        return orderMapper.selectPage(
                new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime));
    }

    /**
     * 按 user_id + order_id 精确路由（库+表都可定位），避免全路由。
     */
    public Order getByUserAndId(long userId, long orderId) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getOrderId, orderId));
    }

    /**
     * 跨分片聚合统计（会广播到 2 库 x 4 表后聚合，仅用于运维场景）。
     */
    public long countAll() {
        return orderMapper.selectCount(null);
    }
}
