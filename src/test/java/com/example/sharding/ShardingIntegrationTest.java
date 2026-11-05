package com.example.sharding;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sharding.entity.AppConfig;
import com.example.sharding.entity.Order;
import com.example.sharding.mapper.AppConfigMapper;
import com.example.sharding.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 集成测试：使用 H2 内存库模拟 2 个分库，验证分库分表路由与单表读写。
 * 无需 MySQL / Docker，mvn test 即可运行。
 */
@SpringBootTest
class ShardingIntegrationTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AppConfigMapper appConfigMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void shardedOrderInsertAndQuery() {
        // 8 个用户各下 1 单：user_id % 2 分布到 2 个库，order_id % 4 分布到各库 4 张表
        for (long userId = 101; userId <= 108; userId++) {
            Order order = new Order();
            order.setUserId(userId);
            order.setAmount(new BigDecimal("99.90"));
            order.setStatus("CREATED");
            order.setCreateTime(LocalDateTime.now());
            orderMapper.insert(order);

            // 雪花主键由 ShardingSphere 生成并回填
            assertThat(order.getOrderId()).isNotNull().isPositive();
        }

        // 全路由聚合统计
        Long total = orderMapper.selectCount(null);
        assertThat(total).isGreaterThanOrEqualTo(8);

        // 主键不重复（雪花算法）
        Set<Long> ids = orderMapper.selectList(null).stream()
                .map(Order::getOrderId)
                .collect(Collectors.toSet());
        assertThat(ids).hasSize(total.intValue());

        // 按 user_id 精确路由查询
        List<Order> ordersOf105 = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, 105L));
        assertThat(ordersOf105).hasSize(1);
        assertThat(ordersOf105.get(0).getUserId()).isEqualTo(105L);
    }

    @Test
    void singleTableConfigCrud() {
        AppConfig config = new AppConfig();
        config.setConfigKey("order.limit.max");
        config.setConfigValue("1000");
        config.setRemark("单用户下单上限");
        config.setUpdateTime(LocalDateTime.now());
        appConfigMapper.insert(config);

        AppConfig loaded = appConfigMapper.selectById("order.limit.max");
        assertThat(loaded).isNotNull();
        assertThat(loaded.getConfigValue()).isEqualTo("1000");

        loaded.setConfigValue("2000");
        appConfigMapper.updateById(loaded);
        assertThat(appConfigMapper.selectById("order.limit.max").getConfigValue()).isEqualTo("2000");
    }
}
