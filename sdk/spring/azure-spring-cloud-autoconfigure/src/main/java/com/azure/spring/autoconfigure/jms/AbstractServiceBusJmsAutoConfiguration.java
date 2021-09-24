// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

/**
 * Abstract autoconfiguration class of ServiceBusJMS for JmsListenerContainerFactory.
 */
public abstract class AbstractServiceBusJmsAutoConfiguration {

    protected final AzureServiceBusJmsProperties azureServiceBusJMSProperties;

    public AbstractServiceBusJmsAutoConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties) {
        this.azureServiceBusJMSProperties = azureServiceBusJMSProperties;
    }

    /**
     * Declare {@link JmsListenerContainerFactory} bean for Azure Service Bus Queue.
     * @param configurer configure {@link DefaultJmsListenerContainerFactory} with sensible defaults
     * @param connectionFactory configure {@link ConnectionFactory} for {@link JmsListenerContainerFactory}
     * @return {@link JmsListenerContainerFactory} bean
     */
    @Bean
    @ConditionalOnMissingBean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        configurer.configure(jmsListenerContainerFactory, connectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.FALSE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    /**
     * Declare {@link JmsListenerContainerFactory} bean for Azure Service Bus Topic.
     * @param configurer configure {@link DefaultJmsListenerContainerFactory} with sensible defaults
     * @param connectionFactory configure {@link ConnectionFactory} for {@link JmsListenerContainerFactory}
     * @return {@link JmsListenerContainerFactory} bean
     */
    @Bean
    @ConditionalOnMissingBean(name = "topicJmsListenerContainerFactory")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        configurer.configure(jmsListenerContainerFactory, connectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.TRUE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        configureTopicListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    private void configureCommonListenerContainerFactory(DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
        AzureServiceBusJmsProperties.Listener listener = azureServiceBusJMSProperties.getListener();
        if (listener.getReplyQosSettings() != null) {
            jmsListenerContainerFactory.setReplyQosSettings(listener.getReplyQosSettings());
        }
        if (listener.getPhase() != null) {
            jmsListenerContainerFactory.setPhase(listener.getPhase());
        }
    }

    private void configureTopicListenerContainerFactory(DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
        AzureServiceBusJmsProperties.Listener listener = azureServiceBusJMSProperties.getListener();
        if (azureServiceBusJMSProperties.getTopicClientId() != null) {
            jmsListenerContainerFactory.setClientId(azureServiceBusJMSProperties.getTopicClientId());
        }
        if (listener.isReplyPubSubDomain() != null) {
            jmsListenerContainerFactory.setReplyPubSubDomain(listener.isReplyPubSubDomain());
        }
        if (listener.isSubscriptionDurable() != null) {
            jmsListenerContainerFactory.setSubscriptionDurable(listener.isSubscriptionDurable());
        }
        if (listener.isSubscriptionShared() != null) {
            jmsListenerContainerFactory.setSubscriptionShared(listener.isSubscriptionShared());
        }
    }
}
