// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;

/**
 * Automatic configuration class of ServiceBusJMS for Standard and Basic Service Bus
 */
@Configuration
@ConditionalOnClass(JmsConnectionFactory.class)
@ConditionalOnResource(resources = "classpath:servicebusjms.enable.config")
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnExpression(value = "not '${spring.jms.servicebus.pricing-tier}'.equalsIgnoreCase('premium')")
@EnableConfigurationProperties(AzureServiceBusJMSProperties.class)
public class NonPremiumServiceBusJMSAutoConfiguration extends AbstractServiceBusJMSAutoConfiguration {

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    /**
     * Creates a new instance of {@link NonPremiumServiceBusJMSAutoConfiguration}.
     *
     * @param azureServiceBusJMSProperties the Azure ServiceBus JMS properties
     */
    public NonPremiumServiceBusJMSAutoConfiguration(AzureServiceBusJMSProperties azureServiceBusJMSProperties) {
        super(azureServiceBusJMSProperties);
    }

    /**
     * Declare JMS ConnectionFactory bean.
     *
     * @return JMS ConnectionFactory bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory() {
        String connectionString = azureServiceBusJMSProperties.getConnectionString();
        String clientId = azureServiceBusJMSProperties.getTopicClientId();
        int idleTimeout = azureServiceBusJMSProperties.getIdleTimeout();
        int durableTopicPrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getDurableTopicPrefetch();
        int queueBrowserPrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getQueueBrowserPrefetch();
        int queuePrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getQueuePrefetch();
        int topicPrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getTopicPrefetch();

        ServiceBusKey serviceBusKey = ConnectionStringResolver.getServiceBusKey(connectionString);
        String host = serviceBusKey.getHost();
        String sasKeyName = serviceBusKey.getSharedAccessKeyName();
        String sasKey = serviceBusKey.getSharedAccessKey();

        String remoteUri = String.format(AMQP_URI_FORMAT, host, idleTimeout);
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
        jmsConnectionFactory.setRemoteURI(remoteUri);
        jmsConnectionFactory.setClientID(clientId);
        jmsConnectionFactory.setUsername(sasKeyName);
        jmsConnectionFactory.setPassword(sasKey);

        JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) jmsConnectionFactory.getPrefetchPolicy();
        prefetchPolicy.setDurableTopicPrefetch(durableTopicPrefetch);
        prefetchPolicy.setQueueBrowserPrefetch(queueBrowserPrefetch);
        prefetchPolicy.setQueuePrefetch(queuePrefetch);
        prefetchPolicy.setTopicPrefetch(topicPrefetch);
        jmsConnectionFactory.setPrefetchPolicy(prefetchPolicy);

        return jmsConnectionFactory;
    }

}
