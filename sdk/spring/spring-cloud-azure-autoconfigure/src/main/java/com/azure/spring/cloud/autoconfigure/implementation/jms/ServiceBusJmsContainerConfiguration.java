// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import jakarta.jms.ConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jms.autoconfigure.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableJms.class)
class ServiceBusJmsContainerConfiguration {

    private final AzureServiceBusJmsProperties azureServiceBusJMSProperties;
    private final ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;
    private final Environment environment;

    ServiceBusJmsContainerConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties,
                                       ObjectProvider<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers,
                                       Environment environment) {
        this.azureServiceBusJMSProperties = azureServiceBusJMSProperties;
        this.factoryCustomizers = factoryCustomizers;
        this.environment = environment;
    }

    @Bean
    @ConditionalOnMissingBean
    JmsListenerContainerFactory<?> jmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        // Use the bean ConnectionFactory if it's pooled or cached, otherwise create a dedicated one for receiver
        ConnectionFactory receiverConnectionFactory = getReceiverConnectionFactory(connectionFactory);
        configurer.configure(jmsListenerContainerFactory, receiverConnectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.FALSE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "topicJmsListenerContainerFactory")
    JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        // Use the bean ConnectionFactory if it's pooled or cached, otherwise create a dedicated one for receiver
        ConnectionFactory receiverConnectionFactory = getReceiverConnectionFactory(connectionFactory);
        configurer.configure(jmsListenerContainerFactory, receiverConnectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.TRUE);
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        configureTopicListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    private ConnectionFactory getReceiverConnectionFactory(ConnectionFactory connectionFactory) {
        // Check if pooling or caching was explicitly enabled by the user
        BindResult<Boolean> poolEnabledResult = Binder.get(environment).bind("spring.jms.servicebus.pool.enabled", Boolean.class);
        BindResult<Boolean> cacheEnabledResult = Binder.get(environment).bind("spring.jms.cache.enabled", Boolean.class);
        
        // Only use the bean ConnectionFactory if pooling or caching was explicitly enabled
        // This ensures receiver uses dedicated ServiceBusJmsConnectionFactory when properties are not set
        boolean poolExplicitlyEnabled = poolEnabledResult.isBound() && poolEnabledResult.get();
        boolean cacheExplicitlyEnabled = cacheEnabledResult.isBound() && cacheEnabledResult.get();
        
        if ((poolExplicitlyEnabled && connectionFactory instanceof JmsPoolConnectionFactory) ||
            (cacheExplicitlyEnabled && connectionFactory instanceof CachingConnectionFactory)) {
            return connectionFactory;
        }
        
        // Create a dedicated ServiceBusJmsConnectionFactory for the receiver
        return createServiceBusJmsConnectionFactory();
    }

    private ServiceBusJmsConnectionFactory createServiceBusJmsConnectionFactory() {
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
