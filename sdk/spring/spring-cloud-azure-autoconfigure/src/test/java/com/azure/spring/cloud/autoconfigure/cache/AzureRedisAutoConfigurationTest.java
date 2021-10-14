// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cache;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AzureRedisAutoConfigurationTest {

    private static final String KEY = "KEY";
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final boolean IS_SSL = true;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureRedisAutoConfiguration.class));

    // TODO (xiada): add tests
    /*@Test
    void testAzureRedisDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.redis.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    void testWithoutRedisOperationsClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(RedisOperations.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureRedisProperties.class));
    }

    @Test
    void testAzureRedisPropertiesIllegal() {
        this.contextRunner
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues("spring.cloud.azure.redis.name=")
            .run(context -> assertThrows(IllegalStateException.class,
                () -> context.getBean(AzureRedisProperties.class)));
    }

    @Test
    void testAzureRedisPropertiesConfigured() {
        this.contextRunner
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
        RedisCacheCrud redisCacheManager() {

            RedisCacheCrud redisCacheManager = mock(RedisCacheCrud.class);
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

    }*/
}
