// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.azure.spring.autoconfigure.unity.AzureProperties;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

import static com.azure.spring.autoconfigure.unity.AzureProperties.AZURE_PROPERTY_BEAN_NAME;

/**
 * Automatic configuration class of ServiceBusJMS for Standard and Basic Service Bus
 */
@Configuration
@ConditionalOnClass(JmsConnectionFactory.class)
@ConditionalOnResource(resources = "classpath:servicebusjms.enable.config")
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnExpression(value = "not '${spring.jms.servicebus.pricing-tier}'.equalsIgnoreCase('premium')")
@EnableConfigurationProperties(AzureServiceBusJMSProperties.class)
public class NonPremiumServiceBusJMSAutoConfiguration {

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusJMSProperties serviceBusJMSProperties,
                                                  @Qualifier(AZURE_PROPERTY_BEAN_NAME)AzureProperties azureProperties) {
        final String connectionString = serviceBusJMSProperties.getConnectionString();
        final String clientId = serviceBusJMSProperties.getTopicClientId();
        final int idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        final ServiceBusKey serviceBusKey = ConnectionStringResolver.getServiceBusKey(connectionString);
        final String host = serviceBusKey.getHost();
        final String sasKeyName = serviceBusKey.getSharedAccessKeyName();
        final String sasKey = serviceBusKey.getSharedAccessKey();

        final String remoteUri = String.format(AMQP_URI_FORMAT, host, idleTimeout);
        final JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
        jmsConnectionFactory.setRemoteURI(remoteUri);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setUsername(sasKeyName);
        jmsConnectionFactory.setPassword(sasKey);
        return jmsConnectionFactory;
    }

    @Bean
    @ConditionalOnMissingBean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        jmsListenerContainerFactory.setConnectionFactory(connectionFactory);
        return jmsListenerContainerFactory;
    }

    @Bean
    public JmsListenerContainerFactory<?> topicJmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        final DefaultJmsListenerContainerFactory jmsListenerContainerFactory = new DefaultJmsListenerContainerFactory();
        jmsListenerContainerFactory.setConnectionFactory(connectionFactory);
        jmsListenerContainerFactory.setSubscriptionDurable(Boolean.TRUE);
        return jmsListenerContainerFactory;
    }

}
