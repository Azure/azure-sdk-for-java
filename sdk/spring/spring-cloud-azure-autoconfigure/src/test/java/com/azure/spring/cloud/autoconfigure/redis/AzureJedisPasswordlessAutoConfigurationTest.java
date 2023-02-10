// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis.AzureJedisConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis.AzureRedisCredentialSupplier;
import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import com.azure.spring.cloud.service.implementation.passwordless.AzureRedisPasswordlessProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import redis.clients.jedis.JedisClientConfig;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AzureJedisPasswordlessAutoConfiguration}.
 */
class AzureJedisPasswordlessAutoConfigurationTest {

    private static final String REDIS_SCOPE_GLOBAL = "https://*.cacheinfra.windows.net:10225/appid/.default";
    private static final String REDIS_SCOPE_CHINA = "https://*.cacheinfra.windows.net.china:10225/appid/.default";
    private static final String REDIS_SCOPE_GERMANY = "https://*.cacheinfra.windows.net.germany:10225/appid/.default";
    private static final String REDIS_SCOPE_US_GOVERNMENT = "https://*.cacheinfra.windows.us.government.net:10225/appid/.default";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues(
            "spring.redis.azure.passwordless-enabled = true",
            "spring.redis.username = testuser",
            "spring.redis.host = testhost"
        )
        .withConfiguration(AutoConfigurations.of(AzureJedisPasswordlessAutoConfiguration.class, CustomConfiguration.class));

    @Test
    @SuppressWarnings("unchecked")
    void testCredentialSupplier() {
        this.contextRunner.run((context) -> {
            AzureRedisCredentialSupplier supplier = context.getBean(AzureRedisCredentialSupplier.class);
            Assertions.assertNotNull(supplier);
        });
    }

    @Test
    void testScopes() {

        this.contextRunner.run((context) -> {
            AzureRedisPasswordlessProperties properties = context.getBean(AzureRedisPasswordlessProperties.class);
            Assertions.assertEquals(REDIS_SCOPE_GLOBAL, properties.getScopes());
        });

        this.contextRunner.withPropertyValues("spring.redis.azure.profile.cloud-type = AZURE_CHINA").run((context) -> {
            AzureRedisPasswordlessProperties properties = context.getBean(AzureRedisPasswordlessProperties.class);
            Assertions.assertEquals(REDIS_SCOPE_CHINA, properties.getScopes());
        });

        this.contextRunner.withPropertyValues("spring.redis.azure.profile.cloud-type = AZURE_GERMANY").run((context) -> {
            AzureRedisPasswordlessProperties properties = context.getBean(AzureRedisPasswordlessProperties.class);
            Assertions.assertEquals(REDIS_SCOPE_GERMANY, properties.getScopes());
        });

        this.contextRunner.withPropertyValues("spring.redis.azure.profile.cloud-type = AZURE_US_GOVERNMENT").run((context) -> {
            AzureRedisPasswordlessProperties properties = context.getBean(AzureRedisPasswordlessProperties.class);
            Assertions.assertEquals(REDIS_SCOPE_US_GOVERNMENT, properties.getScopes());
        });

    }

    @Test
    void connectionFactoryDefaultsToJedis() {
        this.contextRunner.run((context) -> assertThat(context.getBean("azureRedisConnectionFactory"))
            .isInstanceOf(AzureJedisConnectionFactory.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetPasswordFromSupplier() {
        AzureRedisCredentialSupplier azureRedisCredentialSupplier = mock(AzureRedisCredentialSupplier.class);
        when(azureRedisCredentialSupplier.get()).thenReturn("fake-password-from-mock-supplier");
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.database:1")
            .withBean("azureRedisCredentialSupplier", AzureRedisCredentialSupplier.class, () -> azureRedisCredentialSupplier, beanDefinition -> beanDefinition.setPrimary(true))
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                RedisStandaloneConfiguration redisStandaloneConfiguration = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(redisStandaloneConfiguration.getDatabase()).isEqualTo(1);
                assertThat(jedisClientConfiguration.isUseSsl()).isFalse();
            });
    }

    @Test
    void testUseSsl() {
        this.contextRunner
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(jedisClientConfiguration.isUseSsl()).isFalse();
            });
    }

    @Test
    void testRedisUrlConfiguration() {
        this.contextRunner
            .withPropertyValues("spring.redis.host:foo", "spring.redis.ssl:true", "spring.redis.url:redis://user:password@example:33")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("example");
                assertThat(standaloneConfig.getPort()).isEqualTo(33);
                assertThat(jedisClientConfiguration.isUseSsl()).isTrue();
            });
    }

    @Test
    void testOverrideUrlRedisConfiguration() {
        this.contextRunner
            .withPropertyValues("spring.redis.host:foo", "spring.redis.password:xyz", "spring.redis.port:1000",
                "spring.redis.ssl:false", "spring.redis.url:rediss://user:password@example:33")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("example");
                assertThat(standaloneConfig.getPort()).isEqualTo(33);
                assertThat(jedisClientConfiguration.isUseSsl()).isTrue();
            });
    }

    @Test
    void testPasswordInUrlWithColon() {
        this.contextRunner.withPropertyValues("spring.redis.url:redis://:pass:word@example:33").run((context) -> {
            AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
            RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);

            assertThat(standaloneConfig.getHostName()).isEqualTo("example");
            assertThat(standaloneConfig.getPort()).isEqualTo(33);
        });
    }

    @Test
    void testPasswordInUrlStartsWithColon() {
        this.contextRunner.withPropertyValues("spring.redis.url:redis://user::pass:word@example:33").run((context) -> {
            AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
            RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);

            assertThat(standaloneConfig.getHostName()).isEqualTo("example");
            assertThat(standaloneConfig.getPort()).isEqualTo(33);
        });
    }

    @Test
    void testRedisConfigurationWithPool() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.jedis.pool.min-idle:1",
                "spring.redis.jedis.pool.max-idle:4", "spring.redis.jedis.pool.max-active:16",
                "spring.redis.jedis.pool.max-wait:2000", "spring.redis.jedis.pool.time-between-eviction-runs:30000")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("foo");
                assertThat(jedisClientConfiguration.getPoolConfig().get().getMinIdle()).isEqualTo(1);
                assertThat(jedisClientConfiguration.getPoolConfig().get().getMaxIdle()).isEqualTo(4);
                assertThat(jedisClientConfiguration.getPoolConfig().get().getMaxTotal()).isEqualTo(16);
                assertThat(jedisClientConfiguration.getPoolConfig().get().getMaxWaitMillis()).isEqualTo(Duration.ofSeconds(2).toMillis());
                assertThat(jedisClientConfiguration.getPoolConfig().get().getTimeBetweenEvictionRunsMillis()).isEqualTo(Duration.ofSeconds(30).toMillis());
            });
    }

    @Test
    void testRedisConfigurationDisabledPool() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.jedis.pool.enabled:false")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("foo");
                assertThat(jedisClientConfiguration.isUsePooling()).isEqualTo(false);
            });
    }

    @Test
    void testRedisConfigurationWithTimeoutAndConnectTimeout() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.timeout:250",
                "spring.redis.connect-timeout:1000")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("foo");
                assertThat(jedisClientConfiguration.getConnectTimeout().toMillis()).isEqualTo(1000);
            });
    }

    @Test
    void testRedisConfigurationWithDefaultTimeouts() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);
                JedisClientConfiguration jedisClientConfiguration = (JedisClientConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "clientConfiguration", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("foo");
                assertThat(jedisClientConfiguration.getConnectTimeout().toMillis()).isEqualTo(2000);
            });
    }

    @Test
    void testRedisConfigurationWithClientName() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.client-name:spring-boot")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                JedisClientConfig jedisClientConfig = (JedisClientConfig) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "jedisClientConfig", cf);
                RedisStandaloneConfiguration standaloneConfig = (RedisStandaloneConfiguration) ReflectionUtils.getField(AzureJedisConnectionFactory.class, "standaloneConfig", cf);

                assertThat(standaloneConfig.getHostName()).isEqualTo("foo");
                assertThat(jedisClientConfig.getClientName()).isEqualTo("spring-boot");
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomConfiguration {

        @Bean
        AzureGlobalProperties azureGlobalProperties() {
            return new AzureGlobalProperties();
        }
    }

}
