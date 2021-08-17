// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cache;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.spring.cloud.autoconfigure.context.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.cloud.context.core.impl.RedisCacheManager;
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

import java.util.Arrays;

/**
 * An auto-configuration for Spring cache using Azure redis cache
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.redis.enabled", matchIfMissing = true)
@ConditionalOnClass({RedisOperations.class, RedisCacheManager.class})
@EnableConfigurationProperties(AzureRedisProperties.class)
public class AzureRedisAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ AzureResourceManager.class, AzureResourceMetadata.class })
    public RedisCacheManager redisCacheManager(AzureResourceManager azureResourceManager,
                                               AzureResourceMetadata azureResourceMetadata) {
        return new RedisCacheManager(azureResourceManager, azureResourceMetadata);
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public RedisProperties redisProperties(AzureRedisProperties azureRedisProperties,
                                           RedisCacheManager redisCacheManager) {
        String cacheName = azureRedisProperties.getName();

        RedisCache redisCache = redisCacheManager.getOrCreate(cacheName);

        RedisProperties redisProperties = new RedisProperties();

        boolean useSsl = !redisCache.nonSslPort();
        int port = useSsl ? redisCache.sslPort() : redisCache.port();

        boolean isCluster = redisCache.shardCount() > 0;

        if (isCluster) {
            RedisProperties.Cluster cluster = new RedisProperties.Cluster();
            cluster.setNodes(Arrays.asList(redisCache.hostname() + ":" + port));
            redisProperties.setCluster(cluster);
        } else {
            redisProperties.setHost(redisCache.hostname());
            redisProperties.setPort(port);
        }

        redisProperties.setPassword(redisCache.keys().primaryKey());
        redisProperties.setSsl(useSsl);

        return redisProperties;
    }
}
