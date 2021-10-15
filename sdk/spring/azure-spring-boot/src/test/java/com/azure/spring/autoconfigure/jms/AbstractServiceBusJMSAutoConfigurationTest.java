// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.autoconfigure.jms.JmsProperties.AcknowledgeMode.CLIENT;

public abstract class AbstractServiceBusJMSAutoConfigurationTest {

    static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    @Test
    public void testAzureServiceBusJMSPropertiesConnectionStringValidation() {
        ApplicationContextRunner contextRunner = getEmptyContextRunner();
        contextRunner.run(
            context -> Assertions.assertThrows(IllegalStateException.class,
                () -> context.getBean(AzureServiceBusJMSProperties.class)));
    }

    @Test
    public void testConnectionFactoryIsAutowired() {

        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(JmsTemplate.class);
                ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                assertTrue(connectionFactory == context.getBean(JmsTemplate.class).getConnectionFactory());
            }
        );
    }

    @Test
    public void testSpringJmsPropertyConfigured() {

        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(JmsProperties.class);
                JmsProperties jmsProperties = context.getBean(JmsProperties.class);
                assertThat(jmsProperties.getListener().isAutoStartup()).isFalse();
                assertThat(jmsProperties.getListener().getAcknowledgeMode()).isEqualTo(CLIENT);
                assertThat(jmsProperties.getListener().formatConcurrency()).isEqualTo("2-10");
                assertThat(jmsProperties.getListener().getReceiveTimeout()).isEqualTo(Duration.ofSeconds(2));
            }
        );
    }

    @Test
    public void testAzureServiceBusJMSPropertiesConfigured() {

        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJMSProperties.class);
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getConnectionString()).isEqualTo(
                    CONNECTION_STRING);
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getTopicClientId()).isEqualTo("cid");
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getIdleTimeout()).isEqualTo(123);
            }
        );
    }

    @Test
    public void testJMSListenerContainerFactoryConfigured() {
        ApplicationContextRunner contextRunner = getContextRunnerWithProperties();

        contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(DefaultJmsListenerContainerFactoryConfigurer.class);
                assertThat(context).hasBean("jmsListenerContainerFactory");
                assertThat(context).hasBean("topicJmsListenerContainerFactory");

                testQueueJmsListenerContainerFactoryWithCustomSettings(context);
                testTopicJmsListenerContainerFactoryWithCustomSettings(context);
            }
        );
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

    protected abstract ApplicationContextRunner getEmptyContextRunner();

    protected abstract ApplicationContextRunner getContextRunnerWithProperties();
}
