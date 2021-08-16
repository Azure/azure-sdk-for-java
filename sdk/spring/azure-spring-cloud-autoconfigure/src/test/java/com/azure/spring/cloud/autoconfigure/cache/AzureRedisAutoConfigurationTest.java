// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cache;

import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.spring.cloud.context.core.impl.RedisCacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureRedisAutoConfigurationTest {
    private static final String KEY = "KEY";
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final boolean IS_SSL = true;

    @Test
    public void testAzureRedisDisabled() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class))
            .withPropertyValues("spring.cloud.azure.redis.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    public void testWithoutRedisOperationsClass() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class))
            .withClassLoader(new FilteredClassLoader(RedisOperations.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    public void testAzureRedisPropertiesIllegal() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues("spring.cloud.azure.redis.name=")
            .run(context -> assertThrows(IllegalStateException.class,
                () -> context.getBean(AzureRedisProperties.class)));
    }

    @Test
    public void testAzureRedisPropertiesConfigured() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues("spring.cloud.azure.redis.name=redis")
            .run(
                context -> {
                    assertThat(context).hasSingleBean(AzureRedisProperties.class);
                    assertThat(context.getBean(AzureRedisProperties.class).getName()).isEqualTo("redis");
                    assertThat(context).hasSingleBean(RedisProperties.class);
                    assertThat(context.getBean(RedisProperties.class).getPassword()).isEqualTo(KEY);
                    assertThat(context.getBean(RedisProperties.class).getHost()).isEqualTo(HOST);
                    assertThat(context.getBean(RedisProperties.class).getPort()).isEqualTo(PORT);
                    assertThat(context.getBean(RedisProperties.class).isSsl()).isEqualTo(IS_SSL);
                });
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        RedisCacheManager redisCacheManager() {

            RedisCacheManager redisCacheManager = mock(RedisCacheManager.class);
            RedisCache redisCache = mock(RedisCache.class);
            RedisAccessKeys accessKeys = mock(RedisAccessKeys.class);
            when(accessKeys.primaryKey()).thenReturn(KEY);
            when(redisCache.hostname()).thenReturn(HOST);
            when(redisCache.nonSslPort()).thenReturn(!IS_SSL);
            when(redisCache.sslPort()).thenReturn(PORT);
            when(redisCache.shardCount()).thenReturn(0);
            when(redisCache.keys()).thenReturn(accessKeys);
            when(redisCacheManager.getOrCreate(isA(String.class))).thenReturn(redisCache);
            return redisCacheManager;
        }

    }
}
