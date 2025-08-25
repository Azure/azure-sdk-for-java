// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import com.azure.servicebus.jms.ConnectionStringBuilder;
import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.connection.CachingConnectionFactory;

import static com.azure.spring.cloud.autoconfigure.implementation.util.SpringPasswordlessPropertiesUtils.enhancePasswordlessProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

public class ServiceBusJmsConnectionFactoryConfigurationTests {

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

    @ParameterizedTest
    @ValueSource(strings = {"standard", "premium"})
    void useAlternativeConnectionFactoryForPoolConnection(String pricingTier) {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AlternativeConnectionFactoryConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class);
                JmsPoolConnectionFactory wrapper = context.getBean(JmsPoolConnectionFactory.class);
                assertEquals(AlternativeConnectionFactory.class, wrapper.getConnectionFactory().getClass());
            });
    }

    @ParameterizedTest
    @ValueSource(strings = {"standard", "premium"})
    void useAlternativeConnectionFactoryForCacheConnection(String pricingTier) {
        this.contextRunner
            .withConfiguration(AutoConfigurations.of(AlternativeConnectionFactoryConfiguration.class))
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.pool.enabled=false"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
                CachingConnectionFactory wrapper = context.getBean(CachingConnectionFactory.class);
                assertEquals(AlternativeConnectionFactory.class, wrapper.getTargetConnectionFactory().getClass());
            });
    }

    @Configuration
    @PropertySource("classpath:servicebus/additional.properties")
    static class AdditionalPropertySourceConfiguration {

    }

    @Configuration
    static class AlternativeConnectionFactoryConfiguration {

        @Autowired
        AzureServiceBusJmsProperties properties;

        @Bean
        @Primary
        AzureServiceBusJmsConnectionFactoryFactory connectionInstanceFactory() {
            return () -> {
                if (properties.isPasswordlessEnabled()) {
                    String hostName =
                        properties.getNamespace() + "." + properties.getProfile().getEnvironment().getServiceBusDomainName();
                    Properties passwordlessProperties = properties.toPasswordlessProperties();
                    enhancePasswordlessProperties(AzureServiceBusJmsProperties.PREFIX, properties, passwordlessProperties);
                    TokenCredentialProvider tokenCredentialProvider = TokenCredentialProvider.createDefault(new TokenCredentialProviderOptions(passwordlessProperties));
                    TokenCredential tokenCredential = tokenCredentialProvider.get();
                    return new AlternativeConnectionFactory(tokenCredential, hostName, new ServiceBusJmsConnectionFactorySettings());
                } else {
                    return new AlternativeConnectionFactory(properties.getConnectionString(), new ServiceBusJmsConnectionFactorySettings());
                }
            };
        }
    }

    // declare the "access chain" public or face a from checkstyle violation or a
    // java.lang.NoSuchMethodException when running the tests from the command line
    public static class AlternativeConnectionFactory extends ServiceBusJmsConnectionFactory {

        public AlternativeConnectionFactory() {
        }

        public AlternativeConnectionFactory(String connectionString, ServiceBusJmsConnectionFactorySettings settings) {
            super(connectionString, settings);
        }

        public AlternativeConnectionFactory(ConnectionStringBuilder connectionStringBuilder, ServiceBusJmsConnectionFactorySettings settings) {
            super(connectionStringBuilder, settings);
        }

        public AlternativeConnectionFactory(String sasKeyName, String sasKey, String host, ServiceBusJmsConnectionFactorySettings settings) {
            super(sasKey, sasKey, host, settings);
        }

        public AlternativeConnectionFactory(TokenCredential credential, String host, ServiceBusJmsConnectionFactorySettings settings) {
            super(credential, host, settings);
        }
    }
}

