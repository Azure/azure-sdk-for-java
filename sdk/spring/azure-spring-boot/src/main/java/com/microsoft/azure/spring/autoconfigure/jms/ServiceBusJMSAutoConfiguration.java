// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.jms;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.MessageListenerContainer;

import javax.jms.ConnectionFactory;

/**
 * Auto-configuration for Service Bus JMS.
 * <p>
 * The configuration will not be activated if no {@literal spring.jms.servicebus.enabled} property provided.
 */
@Configuration
@ConditionalOnClass(ServiceBusJmsConnectionFactory.class)
@ConditionalOnResource(resources = "classpath:servicebusjms.enable.config")
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusJMSProperties.class)
public class ServiceBusJMSAutoConfiguration {

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusJMSProperties serviceBusJMSProperties) {
        final String connectionString = serviceBusJMSProperties.getConnectionString();
        final String clientID = serviceBusJMSProperties.getTopicClientId();
        final long idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        final ServiceBusJmsConnectionFactorySettings settings =
            new ServiceBusJmsConnectionFactorySettings(idleTimeout, false);
        final ServiceBusJmsConnectionFactory serviceBusJmsConnectionFactory =
            new ServiceBusJmsConnectionFactory(connectionString, settings);

        serviceBusJmsConnectionFactory.setClientId(clientID);

        return new CachingConnectionFactory(serviceBusJmsConnectionFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory) {
        final JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(jmsConnectionFactory);
        return jmsTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsListenerContainerFactory<? extends MessageListenerContainer> jmsListenerContainerFactory(
        ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        jmsListenerContainerFactory.setConnectionFactory(connectionFactory);
        return jmsListenerContainerFactory;
    }

    @Bean
    public JmsListenerContainerFactory<? extends MessageListenerContainer> topicJmsListenerContainerFactory(
        ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        jmsListenerContainerFactory.setConnectionFactory(connectionFactory);
        jmsListenerContainerFactory.setSubscriptionDurable(Boolean.TRUE);
        return jmsListenerContainerFactory;
    }

}
