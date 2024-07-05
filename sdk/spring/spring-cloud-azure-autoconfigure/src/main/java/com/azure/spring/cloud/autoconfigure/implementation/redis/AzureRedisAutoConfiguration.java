// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.spring.cloud.autoconfigure.implementation.redis.properties.AzureRedisProperties;
import com.azure.spring.cloud.autoconfigure.implementation.resourcemanager.AzureResourceManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Cache using Azure Redis Cache support.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(AzureResourceManagerAutoConfiguration.class)
@ConditionalOnExpression("${spring.cloud.azure.redis.enabled:true}")
@ConditionalOnProperty({ "spring.cloud.azure.redis.name", "spring.cloud.azure.redis.resource.resource-group" })
@ConditionalOnClass({ RedisOperations.class, AzureResourceManager.class })
@ConditionalOnBean(AzureResourceManager.class)
@EnableConfigurationProperties(AzureRedisProperties.class)
public class AzureRedisAutoConfiguration {

    @Primary
    @Bean
    RedisProperties redisProperties(AzureRedisProperties azureRedisProperties,
                                    AzureResourceManager azureResourceManager) throws InvocationTargetException,
        IllegalAccessException {
        String cacheName = azureRedisProperties.getName();

        String resourceGroup = azureRedisProperties.getResource().getResourceGroup();
        RedisCache redisCache = azureResourceManager.redisCaches()
                                                    .getByResourceGroup(resourceGroup, cacheName);

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
        Method setSsl = ReflectionUtils.findMethod(RedisProperties.class, "setSsl", boolean.class);
        if (setSsl == null) {
            Object ssl = ReflectionUtils.findMethod(RedisProperties.class, "getSsl").invoke(redisProperties);
            Class<?>[] innerClasses = RedisProperties.class.getDeclaredClasses();
            Class<?> targetInnerClass = null;
            for (Class<?> innerClass : innerClasses) {
                if (innerClass.getSimpleName().equals("Ssl")) {
                    targetInnerClass = innerClass;
                    break;
                }
            }
            ReflectionUtils.findMethod(targetInnerClass, "setEnabled", boolean.class)
                           .invoke(ssl, useSsl);
        } else {
            setSsl.invoke(redisProperties, useSsl);
        }

        return redisProperties;
    }


}
