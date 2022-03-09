// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import java.util.LinkedHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * Creates a new instance of {@link PremiumServiceBusJMSAutoConfiguration}.
     *
     * @param azureServiceBusJMSProperties the Azure ServiceBus JMS properties
     */
    public PremiumServiceBusJMSAutoConfiguration(AzureServiceBusJMSProperties azureServiceBusJMSProperties) {
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
        int prefetchPolicyAll = azureServiceBusJMSProperties.getPrefetchPolicy().getAll();
        int durableTopicPrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getDurableTopicPrefetch();
        int queueBrowserPrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getQueueBrowserPrefetch();
        int queuePrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getQueuePrefetch();
        int topicPrefetch = azureServiceBusJMSProperties.getPrefetchPolicy().getTopicPrefetch();

        ServiceBusJmsConnectionFactorySettings settings =
            new ServiceBusJmsConnectionFactorySettings(new LinkedHashMap<>());
        settings.setConnectionIdleTimeoutMS(idleTimeout);
        settings.setTraceFrames(false);
        settings.setShouldReconnect(false);
        settings.getConfigurationOptions()
            .put("jms.prefetchPolicy.all", String.valueOf(prefetchPolicyAll));
        settings.getConfigurationOptions()
            .put("jms.prefetchPolicy.durableTopicPrefetch", String.valueOf(durableTopicPrefetch));
        settings.getConfigurationOptions()
            .put("jms.prefetchPolicy.queueBrowserPrefetch", String.valueOf(queueBrowserPrefetch));
        settings.getConfigurationOptions()
            .put("jms.prefetchPolicy.queuePrefetch", String.valueOf(queuePrefetch));
        settings.getConfigurationOptions()
            .put("jms.prefetchPolicy.topicPrefetch", String.valueOf(topicPrefetch));
        SpringServiceBusJmsConnectionFactory springServiceBusJmsConnectionFactory =
            new SpringServiceBusJmsConnectionFactory(connectionString, settings);
        springServiceBusJmsConnectionFactory.setClientId(clientId);
        springServiceBusJmsConnectionFactory.setCustomUserAgent(AZURE_SPRING_SERVICE_BUS);

        return springServiceBusJmsConnectionFactory;
    }

}
