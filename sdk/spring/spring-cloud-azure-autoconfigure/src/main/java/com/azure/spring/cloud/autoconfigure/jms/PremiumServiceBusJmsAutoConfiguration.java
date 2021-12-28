// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.ConnectionFactory;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Automatic configuration class of ServiceBusJMS for Premium Service Bus
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusJmsConnectionFactory.class)
@ConditionalOnProperty(value = "spring.jms.servicebus.enabled", matchIfMissing = true)
@ConditionalOnExpression(value = "'${spring.jms.servicebus.pricing-tier}'.equalsIgnoreCase('premium')")
@EnableConfigurationProperties(AzureServiceBusJmsProperties.class)
public class PremiumServiceBusJmsAutoConfiguration extends AbstractServiceBusJmsAutoConfiguration {

    /**
     * Creates a new instance of {@link PremiumServiceBusJmsAutoConfiguration}.
     *
     * @param azureServiceBusJMSProperties the Azure ServiceBus JMS properties
     */
    public PremiumServiceBusJmsAutoConfiguration(AzureServiceBusJmsProperties azureServiceBusJMSProperties) {
        super(azureServiceBusJMSProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionFactory jmsConnectionFactory(AzureServiceBusJmsProperties serviceBusJMSProperties) {
        final String connectionString = serviceBusJMSProperties.getConnectionString();
        final String clientId = serviceBusJMSProperties.getTopicClientId();
        final int idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        ServiceBusJmsConnectionFactorySettings settings =
            new ServiceBusJmsConnectionFactorySettings(new LinkedHashMap<>());
        settings.setConnectionIdleTimeoutMS(idleTimeout);
        settings.setTraceFrames(false);
        settings.setShouldReconnect(false);
        configurePrefetch(serviceBusJMSProperties, settings.getConfigurationOptions());

        SpringServiceBusJmsConnectionFactory springServiceBusJmsConnectionFactory =
            new SpringServiceBusJmsConnectionFactory(connectionString, settings);
        springServiceBusJmsConnectionFactory.setClientId(clientId);
        springServiceBusJmsConnectionFactory.setCustomUserAgent(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);

        return springServiceBusJmsConnectionFactory;
    }

    private void configurePrefetch(AzureServiceBusJmsProperties serviceBusJMSProperties,
                                   Map<String, String> configurationOptions) {
        AzureServiceBusJmsProperties.PrefetchPolicy prefetchPolicy = serviceBusJMSProperties.getPrefetchPolicy();
        int prefetchPolicyAll = prefetchPolicy.getAll();
        int durableTopicPrefetch = prefetchPolicy.getDurableTopicPrefetch();
        int queueBrowserPrefetch = prefetchPolicy.getQueueBrowserPrefetch();
        int queuePrefetch = prefetchPolicy.getQueuePrefetch();
        int topicPrefetch = prefetchPolicy.getTopicPrefetch();
        configurationOptions.put("jms.prefetchPolicy.all", String.valueOf(prefetchPolicyAll));
        configurationOptions.put("jms.prefetchPolicy.durableTopicPrefetch", String.valueOf(durableTopicPrefetch));
        configurationOptions.put("jms.prefetchPolicy.queueBrowserPrefetch", String.valueOf(queueBrowserPrefetch));
        configurationOptions.put("jms.prefetchPolicy.queuePrefetch", String.valueOf(queuePrefetch));
        configurationOptions.put("jms.prefetchPolicy.topicPrefetch", String.valueOf(topicPrefetch));
    }

}
