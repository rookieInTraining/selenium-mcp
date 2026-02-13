package com.autom8tr.selenium_mcp.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Health indicator for Redis connectivity.
 * Only active when distributed mode is enabled.
 */
@Component
@ConditionalOnProperty(name = "selenium.session.distributed-enabled", havingValue = "true")
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;
    private final RedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(StringRedisTemplate redisTemplate, RedisConnectionFactory connectionFactory) {
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try {
            // Ping Redis
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up()
                        .withDetail("status", "Connected")
                        .withDetail("ping", "PONG")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Unexpected response")
                        .withDetail("ping", pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}

