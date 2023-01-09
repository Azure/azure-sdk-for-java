// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis.passwordless.jedis;

import com.azure.spring.cloud.service.implementation.passwordless.AzureRedisPasswordlessProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AzureJedisPasswordlessAutoConfiguration} when Lettuce is not on the classpath.
 */
class AzureJedisPasswordlessAutoConfigurationTest {
    private static final String AZURE_REDIS_CREDENTIAL_SUPPLIER_BEAN_NAME = "azureRedisCredentialSupplier";

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
        .withConfiguration(AutoConfigurations.of(AzureJedisPasswordlessAutoConfiguration.class));

    @Test
    @SuppressWarnings("unchecked")
    void testCredentialSupplier() {
        this.contextRunner.run((context) -> {
            Supplier<String> supplier = (Supplier<String>) context.getBean(AZURE_REDIS_CREDENTIAL_SUPPLIER_BEAN_NAME);
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
        Supplier<String> mockCredentialSupplier = mock(Supplier.class);
        when(mockCredentialSupplier.get()).thenReturn("fake-password-from-mock-supplier");
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.database:1")
            .withBean("azureRedisCredentialSupplier", Supplier.class, () -> mockCredentialSupplier, beanDefinition -> beanDefinition.setPrimary(true))
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("foo");
                assertThat(cf.getDatabase()).isEqualTo(1);
                assertThat(cf.getPassword()).isEqualTo("fake-password-from-mock-supplier");
                assertThat(cf.isUseSsl()).isTrue();
            });
    }

    @Test
    void testUseSsl() {
        this.contextRunner.run((context) -> {
            AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
            assertThat(cf.isUseSsl()).isTrue();
        });
    }

    @Test
    void testRedisUrlConfiguration() {
        this.contextRunner
            .withPropertyValues("spring.redis.host:foo", "spring.redis.url:redis://user:password@example:33")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("example");
                assertThat(cf.getPort()).isEqualTo(33);
                assertThat(cf.getPassword()).isEqualTo("password");
                assertThat(cf.isUseSsl()).isTrue();
            });
    }

    @Test
    void testOverrideUrlRedisConfiguration() {
        this.contextRunner
            .withPropertyValues("spring.redis.host:foo", "spring.redis.password:xyz", "spring.redis.port:1000",
                "spring.redis.ssl:false", "spring.redis.url:rediss://user:password@example:33")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("example");
                assertThat(cf.getPort()).isEqualTo(33);
                assertThat(cf.getPassword()).isEqualTo("password");
                assertThat(cf.isUseSsl()).isTrue();
            });
    }

    @Test
    void testPasswordInUrlWithColon() {
        this.contextRunner.withPropertyValues("spring.redis.url:redis://:pass:word@example:33").run((context) -> {
            AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
            assertThat(cf.getHostName()).isEqualTo("example");
            assertThat(cf.getPort()).isEqualTo(33);
            assertThat(cf.getPassword()).isEqualTo("pass:word");
        });
    }

    @Test
    void testPasswordInUrlStartsWithColon() {
        this.contextRunner.withPropertyValues("spring.redis.url:redis://user::pass:word@example:33").run((context) -> {
            AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
            assertThat(cf.getHostName()).isEqualTo("example");
            assertThat(cf.getPort()).isEqualTo(33);
            assertThat(cf.getPassword()).isEqualTo(":pass:word");
        });
    }

    @Test
    void testRedisConfigurationWithPool() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.jedis.pool.min-idle:1",
                "spring.redis.jedis.pool.max-idle:4", "spring.redis.jedis.pool.max-active:16",
                "spring.redis.jedis.pool.max-wait:2000", "spring.redis.jedis.pool.time-between-eviction-runs:30000")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("foo");
                assertThat(cf.getPoolConfig().getMinIdle()).isEqualTo(1);
                assertThat(cf.getPoolConfig().getMaxIdle()).isEqualTo(4);
                assertThat(cf.getPoolConfig().getMaxTotal()).isEqualTo(16);
                assertThat(cf.getPoolConfig().getMaxWaitDuration()).isEqualTo(Duration.ofSeconds(2));
                assertThat(cf.getPoolConfig().getDurationBetweenEvictionRuns()).isEqualTo(Duration.ofSeconds(30));
            });
    }

    @Test
    void testRedisConfigurationDisabledPool() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.jedis.pool.enabled:false")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("foo");
                assertThat(cf.getClientConfiguration().isUsePooling()).isEqualTo(false);
            });
    }

    @Test
    void testRedisConfigurationWithTimeoutAndConnectTimeout() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.timeout:250",
                "spring.redis.connect-timeout:1000")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("foo");
                assertThat(cf.getTimeout()).isEqualTo(250);
                assertThat(cf.getClientConfiguration().getConnectTimeout().toMillis()).isEqualTo(1000);
            });
    }

    @Test
    void testRedisConfigurationWithDefaultTimeouts() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("foo");
                assertThat(cf.getTimeout()).isEqualTo(2000);
                assertThat(cf.getClientConfiguration().getConnectTimeout().toMillis()).isEqualTo(2000);
            });
    }

    @Test
    void testRedisConfigurationWithClientName() {
        this.contextRunner.withPropertyValues("spring.redis.host:foo", "spring.redis.client-name:spring-boot")
            .run((context) -> {
                AzureJedisConnectionFactory cf = context.getBean(AzureJedisConnectionFactory.class);
                assertThat(cf.getHostName()).isEqualTo("foo");
                assertThat(cf.getClientName()).isEqualTo("spring-boot");
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomConfiguration {

        @Bean
        JedisClientConfigurationBuilderCustomizer customizer() {
            return JedisClientConfigurationBuilder::useSsl;
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class JedisConnectionFactoryCaptorConfiguration {

        @Bean
        JedisConnectionFactoryCaptor jedisConnectionFactoryCaptor() {
            return new JedisConnectionFactoryCaptor();
        }

    }

    static class JedisConnectionFactoryCaptor implements BeanPostProcessor {

        static JedisConnectionFactory connectionFactory;

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            if (bean instanceof JedisConnectionFactory) {
                connectionFactory = (JedisConnectionFactory) bean;
            }
            return bean;
        }

    }

}
