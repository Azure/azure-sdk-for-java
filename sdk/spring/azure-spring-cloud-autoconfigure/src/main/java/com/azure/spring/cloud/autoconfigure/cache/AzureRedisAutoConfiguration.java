// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cache;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.crud.RedisCacheCrud;
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
@ConditionalOnClass({RedisOperations.class, RedisCacheCrud.class})
@EnableConfigurationProperties(AzureRedisProperties.class)
public class AzureRedisAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ AzureResourceManager.class, AzureResourceMetadata.class })
    public RedisCacheCrud redisCacheManager(AzureResourceManager azureResourceManager,
                                            AzureResourceMetadata azureResourceMetadata) {
        return new RedisCacheCrud(azureResourceManager, azureResourceMetadata);
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public RedisProperties redisProperties(AzureRedisProperties azureRedisProperties,
                                           RedisCacheCrud redisCacheManager) {
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
