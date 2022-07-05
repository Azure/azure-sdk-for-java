// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.autoconfigure.jms.JmsProperties.AcknowledgeMode.CLIENT;

class ServiceBusJmsAutoConfigurationTests {

    static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(JmsAutoConfiguration.class, ServiceBusJmsAutoConfiguration.class));

    private void testQueueJmsListenerContainerFactoryWithCustomSettings(AssertableApplicationContext loaded) {
        DefaultJmsListenerContainerFactory listenerContainerFactory =
            (DefaultJmsListenerContainerFactory) loaded.getBean("jmsListenerContainerFactory");
        DefaultMessageListenerContainer container =
            listenerContainerFactory.createListenerContainer(mock(JmsListenerEndpoint.class));
        assertThat(container.isPubSubDomain()).isFalse().as("Pub sub domain should be false.");
        assertThat(container.isAutoStartup()).isFalse();
        assertThat(container.getSessionAcknowledgeMode()).isEqualTo(Session.CLIENT_ACKNOWLEDGE);
        assertThat(container.getConcurrentConsumers()).isEqualTo(2);
        assertThat(container.getMaxConcurrentConsumers()).isEqualTo(10);
        assertThat(container).hasFieldOrPropertyWithValue("receiveTimeout", 2000L);
        assertThat(container.isReplyPubSubDomain()).isFalse();
        assertThat(container.getReplyQosSettings().getPriority()).isEqualTo(1);
        assertThat(container.isSubscriptionDurable()).isFalse();
        assertThat(container.isSubscriptionShared()).isFalse();
        assertThat(container.getPhase()).isEqualTo(2147483647);
        assertThat(container.getClientId()).isNull();
    }

    private void testTopicJmsListenerContainerFactoryWithCustomSettings(AssertableApplicationContext loaded) {
        DefaultJmsListenerContainerFactory listenerContainerFactory =
            (DefaultJmsListenerContainerFactory) loaded.getBean("topicJmsListenerContainerFactory");
        DefaultMessageListenerContainer container =
            listenerContainerFactory.createListenerContainer(mock(JmsListenerEndpoint.class));
        assertThat(container.isPubSubDomain()).isTrue().as("Pub sub domain should be true.");
        assertThat(container.isAutoStartup()).isFalse();
        assertThat(container.getSessionAcknowledgeMode()).isEqualTo(Session.CLIENT_ACKNOWLEDGE);
        assertThat(container.getConcurrentConsumers()).isEqualTo(2);
        assertThat(container.getMaxConcurrentConsumers()).isEqualTo(10);
        assertThat(container).hasFieldOrPropertyWithValue("receiveTimeout", 2000L);
        assertThat(container.isReplyPubSubDomain()).isFalse();
        assertThat(container.getReplyQosSettings().getPriority()).isEqualTo(1);
        assertThat(container.isSubscriptionDurable()).isTrue();
        assertThat(container.isSubscriptionShared()).isFalse();
        assertThat(container.getPhase()).isEqualTo(2147483647);
    }

    @ParameterizedTest
    @ValueSource(classes = { ConnectionFactory.class, JmsConnectionFactory.class, JmsTemplate.class })
    void autoconfigurationNotEnabled(Class<?> clz) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(clz))
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class);
                assertThat(context).doesNotHaveBean(ServiceBusJmsAutoConfiguration.class);
            });
    }

    @Test
    void contextFailedByConnectionStringNotConfigured() {
        this.contextRunner
            .run(context ->
                assertThrows(IllegalStateException.class,
                    () -> context.getBean(AzureServiceBusJmsProperties.class)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Ba", " " })
    void contextFailedByPricingTierNotCorrectlyConfigured(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context ->
                assertThrows(IllegalStateException.class,
                    () -> context.getBean(AzureServiceBusJmsProperties.class))
            );

    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard" })
    void autoconfigurationEnabledAndContextSuccessWithNonpremiumTier(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class);
                assertThat(context).hasSingleBean(ServiceBusJmsAutoConfiguration.class);
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(JmsTemplate.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactoryConfigurer.class);
                assertThat(context).hasBean("jmsListenerContainerFactory");
                assertThat(context).hasBean("topicJmsListenerContainerFactory");
                assertThat(context).doesNotHaveBean("amqpOpenPropertiesCustomizer");
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "premium" })
    void autoconfigurationEnabledAndContextSuccessWithPremiumTier(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class);
                assertThat(context).hasSingleBean(ServiceBusJmsAutoConfiguration.class);
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(JmsTemplate.class);
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactoryConfigurer.class);
                assertThat(context).hasBean("jmsListenerContainerFactory");
                assertThat(context).hasBean("topicJmsListenerContainerFactory");
                assertThat(context).hasBean("amqpOpenPropertiesCustomizer");
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void autoconfigurationDisabled(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.enabled=false",
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class);
            });
    }

    @Test
    void autoconfigurationDisabledCase2() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void doesnotHaveBeanOfAzureServiceBusJmsPropertiesBeanPostProcessor(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureServiceBusJmsPropertiesBeanPostProcessor.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic" })
    void doesHaveBeanOfAzureServiceBusJmsPropertiesBeanPostProcessor(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier)
            .withUserConfiguration(UserMockConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsPropertiesBeanPostProcessor.class);
                String actual = context.getBean(AzureServiceBusJmsProperties.class).getConnectionString();
                assertThat(actual).isEqualTo(CONNECTION_STRING);
            });
    }

    @Configuration
    static class UserMockConfiguration {
        @Bean
        StaticConnectionStringProvider<AzureServiceType.ServiceBus> connectionStringProvider() {
            return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS, CONNECTION_STRING);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void connectionFactoryIsAutowiredIntoJmsTemplateBean(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                assertSame(connectionFactory, context.getBean(JmsTemplate.class).getConnectionFactory());
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void jmsPropertiesConfiguredCorrectly(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.listener.autoStartup=false",
                "spring.jms.listener.acknowledgeMode=client",
                "spring.jms.listener.concurrency=2",
                "spring.jms.listener.receiveTimeout=2s",
                "spring.jms.listener.maxConcurrency=10",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING
            )
            .run(context -> {
                assertThat(context).hasSingleBean(JmsProperties.class);
                JmsProperties jmsProperties = context.getBean(JmsProperties.class);
                assertThat(jmsProperties.getListener().isAutoStartup()).isFalse();
                assertThat(jmsProperties.getListener().getAcknowledgeMode()).isEqualTo(CLIENT);
                assertThat(jmsProperties.getListener().formatConcurrency()).isEqualTo("2-10");
                assertThat(jmsProperties.getListener().getReceiveTimeout()).isEqualTo(Duration.ofSeconds(2));
                assertThat(jmsProperties.getListener().getMaxConcurrency()).isEqualTo(10);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void jmsServiceBusPropertiesConfigured(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123s")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class);
                assertThat(context.getBean(AzureServiceBusJmsProperties.class).getConnectionString()).isEqualTo(CONNECTION_STRING);
                assertThat(context.getBean(AzureServiceBusJmsProperties.class).getTopicClientId()).isEqualTo("cid");
                assertThat(context.getBean(AzureServiceBusJmsProperties.class).getIdleTimeout().getSeconds()).isEqualTo(123);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void jmsListenerContainerFactoryConfiguredCorrectly(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.listener.autoStartup=false",
                "spring.jms.listener.acknowledgeMode=client",
                "spring.jms.listener.concurrency=2",
                "spring.jms.listener.receiveTimeout=2s",
                "spring.jms.listener.maxConcurrency=10",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123",
                "spring.jms.servicebus.listener.reply-pub-sub-domain=false",
                "spring.jms.servicebus.listener.reply-qos-settings.priority=1")
            .run(context -> {
                testQueueJmsListenerContainerFactoryWithCustomSettings(context);
                testTopicJmsListenerContainerFactoryWithCustomSettings(context);
            });
    }


    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void cachingConnectionFactoryBeanConfiguredAsDefault(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
                assertThat(context).doesNotHaveBean(ServiceBusJmsConnectionFactory.class);
                assertThat(context).doesNotHaveBean(JmsPoolConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void cachingConnectionFactoryBeanConfiguredExplicitly(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.cache.enabled=true",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).hasSingleBean(CachingConnectionFactory.class);
                assertThat(context).doesNotHaveBean(ServiceBusJmsConnectionFactory.class);
                assertThat(context).doesNotHaveBean(JmsPoolConnectionFactory.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void nativeConnectionFactoryBeanConfiguredByProperteyCondition(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.cache.enabled=false"
            )
            .run(context -> {
                    assertThat(context).hasSingleBean(ServiceBusJmsConnectionFactory.class);
                    assertThat(context).doesNotHaveBean(CachingConnectionFactory.class);
                    assertThat(context).doesNotHaveBean(JmsPoolConnectionFactory.class);
                }
            );
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void jmsPoolConnectionFactoryBeanConfiguredByPropertyCondition(String pricingTier) {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.pricing-tier=" + pricingTier,
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.pool.enabled=true"
            )
            .run(context -> {
                    assertThat(context).hasSingleBean(JmsPoolConnectionFactory.class);
                    assertThat(context).doesNotHaveBean(ServiceBusJmsConnectionFactory.class);
                    assertThat(context).doesNotHaveBean(CachingConnectionFactory.class);
                }
            );
    }
}
