// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;

/**
 * Abstract autoconfiguration class of ServiceBusJMS for JmsListenerContainerFactory.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableJms.class)
public class ServiceBusJmsContainerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBusJmsContainerConfiguration.class);

    /**
     * The Azure ServiceBus JMS properties
     */
    private final AzureServiceBusJmsProperties azureServiceBusJMSProperties;

    /**
     * Creates a new instance of {@link ServiceBusJmsContainerConfiguration}.
     *
     * @param azureServiceBusJMSProperties the Azure ServiceBus JMS properties
     */
    public ServiceBusJmsContainerConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties) {
        this.azureServiceBusJMSProperties = azureServiceBusJMSProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        configurer.configure(jmsListenerContainerFactory, connectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.FALSE);
        jmsListenerContainerFactory.setErrorHandler(new DefaultErrorHandler());
        configureCommonListenerContainerFactory(jmsListenerContainerFactory);
        return jmsListenerContainerFactory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "topicJmsListenerContainerFactory")
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        configurer.configure(jmsListenerContainerFactory, connectionFactory);
        jmsListenerContainerFactory.setPubSubDomain(Boolean.TRUE);
        jmsListenerContainerFactory.setErrorHandler(new DefaultErrorHandler());
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

    static class DefaultErrorHandler implements ErrorHandler {

        @Override
        public void handleError(Throwable t) {
            logger.error(t.getCause().getMessage());
        }
    }
}
