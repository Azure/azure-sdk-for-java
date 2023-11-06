// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.cloud.autoconfigure.jms.implementation.ServiceBusJmsConnectionFactoryCustomizer2;
import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.implementation.util.ReflectionUtils;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import org.apache.qpid.jms.JmsConnectionExtensions;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A factory for ServiceBusJmsConnectionFactory.
 */
public class ServiceBusJmsConnectionFactoryFactory {
    private final AzureServiceBusJmsProperties properties;
    private final List<ServiceBusJmsConnectionFactoryCustomizer2> factoryCustomizers;

    ServiceBusJmsConnectionFactoryFactory(AzureServiceBusJmsProperties properties,
                                          List<ServiceBusJmsConnectionFactoryCustomizer2> factoryCustomizers) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
        this.factoryCustomizers = (factoryCustomizers != null) ? factoryCustomizers : Collections.emptyList();

    }

    <T extends ServiceBusJmsConnectionFactory> T createConnectionFactory(Class<T> factoryClass) {
        T factory = createConnectionFactoryInstance(factoryClass);
        setClientId(factory);
        setPrefetchPolicy(factory);
        customize(factory);
        return factory;
    }

    private <T extends ServiceBusJmsConnectionFactory> void setClientId(T factory) {
        if (StringUtils.hasText(this.properties.getTopicClientId())) {
            factory.setClientId(this.properties.getTopicClientId());
        }
    }

    private <T extends ServiceBusJmsConnectionFactory> void setPrefetchPolicy(T factory) {
        AzureServiceBusJmsProperties.PrefetchPolicy prefetchProperties = this.properties.getPrefetchPolicy();
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.durableTopicPrefetch", String.valueOf(prefetchProperties.getDurableTopicPrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.queueBrowserPrefetch", String.valueOf(prefetchProperties.getQueueBrowserPrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.queuePrefetch", String.valueOf(prefetchProperties.getQueuePrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.topicPrefetch", String.valueOf(prefetchProperties.getTopicPrefetch()));
    }

    private <T extends ServiceBusJmsConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass) {
        try {
            T factory;
            if (properties.isPasswordlessEnabled()) {
                String hostName = properties.getNamespace() + "." + properties.getProfile().getEnvironment().getServiceBusDomainName();

                TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

                factory = factoryClass.getConstructor(TokenCredential.class, String.class, ServiceBusJmsConnectionFactorySettings.class)
                                      .newInstance(tokenCredential, hostName, new ServiceBusJmsConnectionFactorySettings());

            } else {
                factory = factoryClass.getConstructor(String.class, ServiceBusJmsConnectionFactorySettings.class).newInstance(properties.getConnectionString(), new ServiceBusJmsConnectionFactorySettings());
            }
            if ("standard".equalsIgnoreCase(properties.getPricingTier())) {
                JmsConnectionFactory jmsFactory = (JmsConnectionFactory) ReflectionUtils.getField(ServiceBusJmsConnectionFactory.class, "factory", factory);
                jmsFactory.setExtension(JmsConnectionExtensions.AMQP_OPEN_PROPERTIES.toString(),
                    (connection, uri) -> {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("com.microsoft:is-client-provider", false);

                        return properties;
                    });
            }
            return factory;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create JmsConnectionFactory", ex);
        }
    }

    private void customize(ServiceBusJmsConnectionFactory connectionFactory) {
        for (ServiceBusJmsConnectionFactoryCustomizer2 factoryCustomizer : this.factoryCustomizers) {
            factoryCustomizer.customize(connectionFactory);
        }
    }
}
