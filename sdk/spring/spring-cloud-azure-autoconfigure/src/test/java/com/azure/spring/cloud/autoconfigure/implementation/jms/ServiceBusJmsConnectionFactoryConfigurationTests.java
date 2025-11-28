// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.connection.CachingConnectionFactory;

import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceBusJmsConnectionFactoryConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
        .withPropertyValues(
            "spring.jms.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
        )
        .withConfiguration(AutoConfigurations.of(JmsAutoConfiguration.class,
            ServiceBusJmsAutoConfiguration.class));

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useDefaultPoolConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier
            )
            .run(context -> {
                assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void enablePoolConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "org.messaginghub.pooled.jms.JmsPoolConnectionFactory", "org.apache.commons.pool2.PooledObject" })
    void poolEnabledButNoPoolClasses(String poolClass) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(poolClass))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium",
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "org.messaginghub.pooled.jms.JmsPoolConnectionFactory", "org.apache.commons.pool2.PooledObject" })
    void fallbackUseCachingConnectionDueNoPoolClasses(String poolClass) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(poolClass))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=premium"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useCacheConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=false"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void fallbackUseDefaultConnectionDueNoPoolAndCachingClasses(String pricingTier) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(
                "org.apache.commons.pool2.PooledObject",
                "org.messaginghub.pooled.jms.JmsPoolConnectionFactory",
                "org.springframework.jms.connection.CachingConnectionFactory"
            ))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier
            )
            .run(context -> {
                assertThat(context).doesNotHaveBean(JmsPoolConnectionFactory.class);
                assertThat(context).doesNotHaveBean(CachingConnectionFactory.class);
                assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useServiceBusJmsConnection(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=false",
                "spring.jms.cache.enabled=false"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "standard", "premium" })
    void useCacheConnectionViaAdditionConfigurationFile(String pricingTier) {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AdditionalPropertySourceConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
            });
    }

    @Configuration
    @PropertySource("classpath:servicebus/additional.properties")
    static class AdditionalPropertySourceConfiguration {

    }
}
