// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
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

abstract class AbstractServiceBusJmsAutoConfigurationTest {

    static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    protected ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JmsAutoConfiguration .class));

    @Test
    void testAzureServiceBusJMSPropertiesConnectionStringValidation() {
        this.contextRunner
            .run(context -> assertThrows(IllegalStateException.class,
                                         () -> context.getBean(AzureServiceBusJmsProperties.class)));
    }

    @Test
    void testConnectionFactoryIsAutowired() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.listener.autoStartup=false",
                "spring.jms.listener.acknowledgeMode=client",
                "spring.jms.listener.concurrency=2",
                "spring.jms.listener.receiveTimeout=2s",
                "spring.jms.listener.maxConcurrency=10",
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
            .run(context -> {
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(JmsTemplate.class);
                ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                assertSame(connectionFactory, context.getBean(JmsTemplate.class).getConnectionFactory());
            });
    }

    @Test
    void testSpringJmsPropertyConfigured() {
        this.contextRunner
            .withPropertyValues(
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

    @Test
    void testAzureServiceBusJMSPropertiesConfigured() {
        this.contextRunner
            .withPropertyValues(
                "spring.jms.servicebus.connection-string=" + CONNECTION_STRING,
                "spring.jms.servicebus.topic-client-id=cid",
                "spring.jms.servicebus.idle-timeout=123")
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class);
                assertThat(context.getBean(AzureServiceBusJmsProperties.class).getConnectionString()).isEqualTo(CONNECTION_STRING);
                assertThat(context.getBean(AzureServiceBusJmsProperties.class).getTopicClientId()).isEqualTo("cid");
                assertThat(context.getBean(AzureServiceBusJmsProperties.class).getIdleTimeout()).isEqualTo(123);
            });
    }

    @Test
    void testJMSListenerContainerFactoryConfigured() {
        this.contextRunner
            .withPropertyValues(
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
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactoryConfigurer.class);
                assertThat(context).hasBean("jmsListenerContainerFactory");
                assertThat(context).hasBean("topicJmsListenerContainerFactory");

                testQueueJmsListenerContainerFactoryWithCustomSettings(context);
                testTopicJmsListenerContainerFactoryWithCustomSettings(context);
            });
    }

    private void testQueueJmsListenerContainerFactoryWithCustomSettings(AssertableApplicationContext loaded) {
        DefaultJmsListenerContainerFactory listenerContainerFactory = (DefaultJmsListenerContainerFactory) loaded.getBean("jmsListenerContainerFactory");
        DefaultMessageListenerContainer container = listenerContainerFactory.createListenerContainer(mock(JmsListenerEndpoint.class));
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
        DefaultJmsListenerContainerFactory listenerContainerFactory = (DefaultJmsListenerContainerFactory) loaded.getBean("topicJmsListenerContainerFactory");
        DefaultMessageListenerContainer container = listenerContainerFactory.createListenerContainer(mock(JmsListenerEndpoint.class));
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
        assertThat(container.getClientId()).isEqualTo("cid");
    }

}
