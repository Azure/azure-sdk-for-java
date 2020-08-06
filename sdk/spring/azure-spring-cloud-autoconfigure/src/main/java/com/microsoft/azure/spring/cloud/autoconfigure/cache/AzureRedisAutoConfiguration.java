// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.cache;

import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisOperations;

import javax.annotation.PostConstruct;
import java.util.Arrays;

/**
 * An auto-configuration for Spring cache using Azure redis cache
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.redis.enabled", matchIfMissing = true)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnBean(ResourceManagerProvider.class)
@EnableConfigurationProperties(AzureRedisProperties.class)
public class AzureRedisAutoConfiguration {
    private static final String REDIS = "Redis";

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(REDIS);
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public RedisProperties redisProperties(ResourceManagerProvider resourceManagerProvider,
                                           AzureRedisProperties azureRedisProperties) {
        String cacheName = azureRedisProperties.getName();

        RedisCache redisCache = resourceManagerProvider.getRedisCacheManager().getOrCreate(cacheName);

        RedisProperties redisProperties = new RedisProperties();

        boolean useSsl = !redisCache.nonSslPort();
        int port = useSsl ? redisCache.sslPort() : redisCache.port();

        boolean isCluster = redisCache.shardCount() > 0;

        if (isCluster) {
            RedisProperties.Cluster cluster = new RedisProperties.Cluster();
            cluster.setNodes(Arrays.asList(redisCache.hostName() + ":" + port));
            redisProperties.setCluster(cluster);
        } else {
            redisProperties.setHost(redisCache.hostName());
            redisProperties.setPort(port);
        }

        redisProperties.setPassword(redisCache.getKeys().primaryKey());
        redisProperties.setSsl(useSsl);

        return redisProperties;
    }
}
