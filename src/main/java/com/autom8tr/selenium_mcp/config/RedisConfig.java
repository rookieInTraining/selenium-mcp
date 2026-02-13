package com.autom8tr.selenium_mcp.config;

import com.autom8tr.selenium_mcp.core.session.HybridSessionManager;
import com.autom8tr.selenium_mcp.core.session.RedisSessionRegistry;
import com.autom8tr.selenium_mcp.core.session.SessionRegistryEntry;
import com.autom8tr.selenium_mcp.core.session.SessionStorage;
import com.autom8tr.selenium_mcp.core.session.InMemorySessionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Redis configuration for distributed session management.
 */
@Configuration
public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * Redis template for session registry entries.
     */
    @Bean
    @ConditionalOnProperty(name = "selenium.session.distributed-enabled", havingValue = "true")
    public RedisTemplate<String, SessionRegistryEntry> sessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SessionRegistryEntry> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis session registry for distributed mode.
     */
    @Bean
    @ConditionalOnProperty(name = "selenium.session.distributed-enabled", havingValue = "true")
    public RedisSessionRegistry redisSessionRegistry(
            RedisTemplate<String, SessionRegistryEntry> sessionRedisTemplate,
            StringRedisTemplate stringRedisTemplate,
            SessionProperties sessionProperties) {

        String podId = resolvePodId(sessionProperties);
        String podAddress = resolvePodAddress(sessionProperties);

        log.info("Creating Redis session registry: podId={}, podAddress={}", podId, podAddress);

        return new RedisSessionRegistry(
                sessionRedisTemplate,
                stringRedisTemplate,
                podId,
                podAddress,
                sessionProperties.getTtl()
        );
    }

    /**
     * Hybrid session manager for distributed mode.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "selenium.session.distributed-enabled", havingValue = "true")
    public SessionStorage hybridSessionStorage(
            RedisSessionRegistry redisRegistry,
            SessionProperties sessionProperties) {

        String podId = resolvePodId(sessionProperties);

        log.info("Creating hybrid session storage: maxSessions={}, ttl={}, podId={}",
                sessionProperties.getMaxSessions(), sessionProperties.getTtl(), podId);

        return new HybridSessionManager(
                redisRegistry,
                sessionProperties.getMaxSessions(),
                sessionProperties.getTtl(),
                podId
        );
    }

    /**
     * In-memory session storage for standalone mode.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "selenium.session.distributed-enabled", havingValue = "false", matchIfMissing = true)
    public SessionStorage inMemorySessionStorage() {
        log.info("Creating in-memory session storage (standalone mode)");
        return new InMemorySessionStorage();
    }

    /**
     * Resolves pod ID from configuration or generates one.
     */
    private String resolvePodId(SessionProperties properties) {
        if (properties.getPodId() != null && !properties.getPodId().isEmpty()) {
            return properties.getPodId();
        }

        // Try to get from Kubernetes environment
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }

        // Generate unique ID
        return "pod-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Resolves pod address from configuration or determines it.
     */
    private String resolvePodAddress(SessionProperties properties) {
        if (properties.getPodAddress() != null && !properties.getPodAddress().isEmpty()) {
            return properties.getPodAddress();
        }

        // Try to get from Kubernetes environment
        String podIp = System.getenv("POD_IP");
        if (podIp != null && !podIp.isEmpty()) {
            return podIp;
        }

        // Try to get local address
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("Could not determine pod address: {}", e.getMessage());
            return "localhost";
        }
    }
}

