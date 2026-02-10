// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableJms.class)
class ServiceBusJmsContainerConfiguration {

    private final AzureServiceBusJmsProperties azureServiceBusJMSProperties;
    private final ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;

    ServiceBusJmsContainerConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties,
                                       ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        this.azureServiceBusJMSProperties = azureServiceBusJMSProperties;
        this.factoryCustomizers = factoryCustomizers;
    }

    @Bean
    @ConditionalOnMissingBean
    JmsListenerContainerFactory<?> jmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        // Create a dedicated ServiceBusJmsConnectionFactory for the receiver side
        ConnectionFactory receiverConnectionFactory = createReceiverConnectionFactory();
        configurer.configure(jmsListenerContainerFactory, receiverConnectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.FALSE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "topicJmsListenerContainerFactory")
    JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        // Create a dedicated ServiceBusJmsConnectionFactory for the receiver side
        ConnectionFactory receiverConnectionFactory = createReceiverConnectionFactory();
        configurer.configure(jmsListenerContainerFactory, receiverConnectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.TRUE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        configureTopicListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    private ServiceBusJmsConnectionFactory createReceiverConnectionFactory() {
        return new ServiceBusJmsConnectionFactoryFactory(azureServiceBusJMSProperties,
            factoryCustomizers.orderedStream().collect(Collectors.toList()))
            .createConnectionFactory(ServiceBusJmsConnectionFactory.class);
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
