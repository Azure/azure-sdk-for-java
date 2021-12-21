// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.core.connectionstring.implementation.ServiceBusConnectionString;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;

/**
 * Automatic configuration class of ServiceBusJMS for Standard and Basic Service Bus
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JmsConnectionFactory.class)
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnExpression(value = "not '${spring.jms.servicebus.pricing-tier}'.equalsIgnoreCase('premium')")
@EnableConfigurationProperties(AzureServiceBusJmsProperties.class)
public class NonPremiumServiceBusJmsAutoConfiguration extends AbstractServiceBusJmsAutoConfiguration {

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    /**
     * Creates a new instance of {@link NonPremiumServiceBusJmsAutoConfiguration}.
     *
     * @param azureServiceBusJMSProperties the Azure ServiceBus JMS properties
     */
    public NonPremiumServiceBusJmsAutoConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties) {
        super(azureServiceBusJMSProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusJmsProperties serviceBusJMSProperties) {
        final String connectionString = serviceBusJMSProperties.getConnectionString();
        final String clientId = serviceBusJMSProperties.getTopicClientId();
        final int idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        ServiceBusConnectionString serviceBusConnectionString = new ServiceBusConnectionString(connectionString);
        String host = serviceBusConnectionString.getEndpointUri().getHost();
        String sasKeyName = serviceBusConnectionString.getSharedAccessKeyName();
        String sasKey = serviceBusConnectionString.getSharedAccessKey();

        String remoteUri = String.format(AMQP_URI_FORMAT, host, idleTimeout);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
        jmsConnectionFactory.setRemoteURI(remoteUri);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setUsername(sasKeyName);
        jmsConnectionFactory.setPassword(sasKey);

        JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) jmsConnectionFactory.getPrefetchPolicy();
        AzureServiceBusJmsProperties.PrefetchPolicy prefetchProperties = serviceBusJMSProperties.getPrefetchPolicy();
        prefetchPolicy.setDurableTopicPrefetch(prefetchProperties.getDurableTopicPrefetch());
        prefetchPolicy.setQueueBrowserPrefetch(prefetchProperties.getQueueBrowserPrefetch());
        prefetchPolicy.setQueuePrefetch(prefetchProperties.getQueuePrefetch());
        prefetchPolicy.setTopicPrefetch(prefetchProperties.getTopicPrefetch());
        jmsConnectionFactory.setPrefetchPolicy(prefetchPolicy);

        return jmsConnectionFactory;
    }

}
