// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

import static com.azure.spring.utils.ApplicationId.AZURE_SPRING_SERVICE_BUS;

/**
 * Automatic configuration class of ServiceBusJMS for Premium Service Bus
 */
@Configuration
@ConditionalOnClass(ServiceBusJmsConnectionFactory.class)
@ConditionalOnResource(resources = "classpath:servicebusjms.enable.config")
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnExpression(value = "'${spring.jms.servicebus.pricing-tier}'.equalsIgnoreCase('premium')")
@EnableConfigurationProperties(AzureServiceBusJMSProperties.class)
public class PremiumServiceBusJMSAutoConfiguration extends AbstractServiceBusJMSAutoConfiguration {

    public PremiumServiceBusJMSAutoConfiguration(JmsProperties jmsProperties,
                                                 AzureServiceBusJMSProperties azureServiceBusJMSProperties) {
        super(jmsProperties, azureServiceBusJMSProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusJMSProperties serviceBusJMSProperties) {
        final String connectionString = serviceBusJMSProperties.getConnectionString();
        final String clientId = serviceBusJMSProperties.getTopicClientId();
        final int idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        final ServiceBusJmsConnectionFactorySettings settings =
            new ServiceBusJmsConnectionFactorySettings(idleTimeout, false);
        settings.setShouldReconnect(false);
        final SpringServiceBusJmsConnectionFactory springServiceBusJmsConnectionFactory =
            new SpringServiceBusJmsConnectionFactory(connectionString, settings);
        springServiceBusJmsConnectionFactory.setClientId(clientId);
        springServiceBusJmsConnectionFactory.setCustomUserAgent(AZURE_SPRING_SERVICE_BUS);

        return springServiceBusJmsConnectionFactory;
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
//        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
//        final DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
//        configurer.configure(jmsListenerContainerFactory, connectionFactory);
//        return jmsListenerContainerFactory;
//    }
//
//    @Bean
//    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(
//        DefaultJmsListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
//        final DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
//        configurer.configure(jmsListenerContainerFactory, connectionFactory);
//        jmsListenerContainerFactory.setSubscriptionDurable(Boolean.TRUE);
//        return jmsListenerContainerFactory;
//    }

}
