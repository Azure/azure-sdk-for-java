// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.properties.ServiceBusJmsProperties;
import com.azure.spring.core.implementation.connectionstring.ServiceBusConnectionString;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.boot.autoconfigure.jms.JndiConnectionFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.core.AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS;

/**
 * An auto-configuration for Service Bus JMS.
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(JmsAutoConfiguration.class)
@AutoConfigureAfter({ JndiConnectionFactoryAutoConfiguration.class })
@ConditionalOnClass({ ConnectionFactory.class, JmsConnectionFactory.class,
    JmsPoolConnectionFactory.class, JmsTemplate.class })
@EnableConfigurationProperties({ ServiceBusJmsProperties.class,
    JmsProperties.class })
@Import({ ServiceBusJmsConnectionFactoryConfiguration.class, ServiceBusJmsContainerConfiguration.class })
public class ServiceBusJmsAutoConfiguration {
    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    @Bean
    ServiceBusJmsConnectionFactoryCustomizer customizer(ServiceBusJmsProperties serviceBusJMSProperties) {
        return new ServiceBusJmsConnectionFactoryCustomizer() {

            @Override
            public void customize(ServiceBusJmsConnectionFactory factory) {
                initializeConnection(factory, serviceBusJMSProperties);

                AzureServiceBusJmsProperties.PrefetchPolicy prefetchProperties = serviceBusJMSProperties
                    .getPrefetchPolicy();
                updatePrefetchPolicy(factory, prefetchProperties);
                if(serviceBusJMSProperties.getPricingTier().equalsIgnoreCase("premium")) {
                    setupUserAgent(factory);
                }
            }
        };
    }

    private void updatePrefetchPolicy(ServiceBusJmsConnectionFactory factory,
                                      AzureServiceBusJmsProperties.PrefetchPolicy prefetchProperties) {
        JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) factory
            .getPrefetchPolicy();
        prefetchPolicy.setDurableTopicPrefetch(prefetchProperties.getDurableTopicPrefetch());
        prefetchPolicy.setQueueBrowserPrefetch(prefetchProperties.getQueueBrowserPrefetch());
        prefetchPolicy.setQueuePrefetch(prefetchProperties.getQueuePrefetch());
        prefetchPolicy.setTopicPrefetch(prefetchProperties.getTopicPrefetch());
        factory.setPrefetchPolicy(prefetchPolicy);
    }

    private void initializeConnection(ServiceBusJmsConnectionFactory jmsConnectionFactory,
                                      ServiceBusJmsProperties serviceBusJMSProperties) {
        final String connectionString = serviceBusJMSProperties.getConnectionString();
        final int idleTimeout = serviceBusJMSProperties.getIdleTimeout();

        ServiceBusConnectionString serviceBusConnectionString = new ServiceBusConnectionString(connectionString);
        String host = serviceBusConnectionString.getEndpointUri().getHost();
        String sasKeyName = serviceBusConnectionString.getSharedAccessKeyName();
        String sasKey = serviceBusConnectionString.getSharedAccessKey();

        String remoteUri = String.format(AMQP_URI_FORMAT, host, idleTimeout);
        jmsConnectionFactory.setRemoteURI(remoteUri);
        jmsConnectionFactory.setUsername(sasKeyName);
        jmsConnectionFactory.setPassword(sasKey);
    }

    private void setupUserAgent(ServiceBusJmsConnectionFactory jmsConnectionFactory) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("com.microsoft:is-client-provider", true);
        String userAgent = "ServiceBusJms-unknown/" + AZURE_SPRING_SERVICE_BUS;
        properties.put("user-agent", userAgent);
        //set user agent
        jmsConnectionFactory.setExtension(JmsConnectionExtensions.AMQP_OPEN_PROPERTIES.toString(),
            (connection, uri) -> properties);
    }
}
