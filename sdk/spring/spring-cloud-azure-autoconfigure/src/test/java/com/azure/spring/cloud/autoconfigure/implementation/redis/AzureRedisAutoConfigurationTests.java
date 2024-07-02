// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.redis.models.RedisAccessKeys;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.spring.cloud.autoconfigure.implementation.redis.properties.AzureRedisProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureRedisAutoConfigurationTests {

    private static final String KEY = "KEY";
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final boolean IS_SSL = true;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class));

    @Test
    void testAzureRedisDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.redis.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureRedisAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureRedisProperties.class);
            });
    }

    @Test
    void testWithoutRedisOperationsClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(RedisOperations.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureRedisAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureRedisProperties.class);
            });
    }

    @Test
    void shouldNotConfigureWithoutRedisName() {
        this.contextRunner
            .withBean(AzureResourceManager.class, this::mockResourceManager)
            .withPropertyValues("spring.cloud.azure.redis.resource.resource-group=rg")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureRedisAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureRedisProperties.class);
            });
    }

    @Test
    void shouldNotConfigureWithoutRedisResourceGroup() {
        this.contextRunner
            .withBean(AzureResourceManager.class, this::mockResourceManager)
            .withPropertyValues("spring.cloud.azure.redis.name=redis")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureRedisAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureRedisProperties.class);
            });
    }

    @Test
    void shouldNotConfigureWithoutAzureResourceManager() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.redis.name=redis",
                "spring.cloud.azure.redis.resource.resource-group=rg"
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureRedisAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureRedisProperties.class);
            });
    }

    @Test
    void shouldConfigureWithNameAndResourceGroupAndResourceManager() {
        this.contextRunner
            .withBean(AzureResourceManager.class, this::mockResourceManager)
            .withPropertyValues(
                "spring.cloud.azure.redis.name=redis",
                "spring.cloud.azure.redis.resource.resource-group=rg"
            )
            .run(
                context -> {
                    assertThat(context).hasSingleBean(AzureRedisAutoConfiguration.class);
                    assertThat(context).hasSingleBean(AzureRedisProperties.class);

                    AzureRedisProperties azureRedisProperties = context.getBean(AzureRedisProperties.class);
                    assertThat(azureRedisProperties.getName()).isEqualTo("redis");
                    assertThat(azureRedisProperties.getResource().getResourceGroup()).isEqualTo("rg");

                    assertThat(context).hasSingleBean(RedisProperties.class);
                    RedisProperties redisProperties = context.getBean(RedisProperties.class);
                    assertThat(redisProperties.getPassword()).isEqualTo(KEY);
                    assertThat(redisProperties.getHost()).isEqualTo(HOST);
                    assertThat(redisProperties.getPort()).isEqualTo(PORT);
                    Method isSsl = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
                    if (isSsl == null) {
                        Object ssl = ReflectionUtils.findMethod(RedisProperties.class, "getSsl").invoke(redisProperties);
                        Class<?>[] innerClasses = RedisProperties.class.getDeclaredClasses();
                        Class<?> targetInnerClass = null;
                        for (Class<?> innerClass : innerClasses) {
                            if (innerClass.getSimpleName().equals("Ssl")) {
                                targetInnerClass = innerClass;
                                break;
                            }
                        }
                        assertThat(ReflectionUtils.findMethod(targetInnerClass, "isEnabled")
                                                  .invoke(ssl).equals(IS_SSL));
                    } else {
                        assertThat(isSsl.invoke(redisProperties).equals(IS_SSL));
                    }
                });
    }

    private AzureResourceManager mockResourceManager() {
        RedisCache redisCache = mock(RedisCache.class);
        RedisAccessKeys accessKeys = mock(RedisAccessKeys.class);
        when(accessKeys.primaryKey()).thenReturn(KEY);
        when(redisCache.hostname()).thenReturn(HOST);
        when(redisCache.nonSslPort()).thenReturn(!IS_SSL);
        when(redisCache.sslPort()).thenReturn(PORT);
        when(redisCache.shardCount()).thenReturn(0);
        when(redisCache.keys()).thenReturn(accessKeys);

        AzureResourceManager mockResourceManager = mock(AzureResourceManager.class);

        RedisCaches mockRedisCaches = mock(RedisCaches.class);
        when(mockResourceManager.redisCaches()).thenReturn(mockRedisCaches);
        when(mockRedisCaches.getByResourceGroup(anyString(), anyString())).thenReturn(redisCache);

        return mockResourceManager;
    }

}
