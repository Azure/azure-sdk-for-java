// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.core.implementation.connectionstring.ServiceBusConnectionString;
import com.azure.spring.cloud.service.implementation.passwordless.AzureServiceBusPasswordlessProperties;
import org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * A factory for ServiceBusJmsConnectionFactory.
 */
public class ServiceBusJmsConnectionFactoryFactory {
    private final AzureServiceBusJmsProperties properties;
    private final List<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;
    private final AzureServiceBusPasswordlessProperties serviceBusPasswordlessProperties;

    private static final String AMQP_URI_FORMAT = "amqps://%s?amqp.idleTimeout=%d";

    ServiceBusJmsConnectionFactoryFactory(AzureServiceBusJmsProperties properties,
                                          List<ServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers,
                                          AzureServiceBusPasswordlessProperties serviceBusPasswordlessProperties) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
        this.serviceBusPasswordlessProperties = serviceBusPasswordlessProperties;
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
            factory.setClientID(this.properties.getTopicClientId());
        }
    }

    private <T extends ServiceBusJmsConnectionFactory> void setPrefetchPolicy(T factory) {
        AzureServiceBusJmsProperties.PrefetchPolicy prefetchProperties = this.properties.getPrefetchPolicy();
        JmsDefaultPrefetchPolicy prefetchPolicy = (JmsDefaultPrefetchPolicy) factory.getPrefetchPolicy();
        prefetchPolicy.setDurableTopicPrefetch(prefetchProperties.getDurableTopicPrefetch());
        prefetchPolicy.setQueueBrowserPrefetch(prefetchProperties.getQueueBrowserPrefetch());
        prefetchPolicy.setQueuePrefetch(prefetchProperties.getQueuePrefetch());
        prefetchPolicy.setTopicPrefetch(prefetchProperties.getTopicPrefetch());
        factory.setPrefetchPolicy(prefetchPolicy);
    }

    private <T extends ServiceBusJmsConnectionFactory> T createConnectionFactoryInstance(Class<T> factoryClass) {
        try {
            T factory;
            String remoteUrl = null;
            String username = null;
            String password = null;
            if (serviceBusPasswordlessProperties != null && properties.getNameSpace() != null) {
                remoteUrl = String.format(AMQP_URI_FORMAT,
                    properties.getNameSpace() + serviceBusPasswordlessProperties.getProfile().getEnvironment().getServiceBusDomainName(),
                    properties.getIdleTimeout().toMillis());
            } else if (properties.getConnectionString() != null) {
                ServiceBusConnectionString serviceBusConnectionString = new ServiceBusConnectionString(properties.getConnectionString());
                String host = serviceBusConnectionString.getEndpointUri().getHost();
                remoteUrl = String.format(AMQP_URI_FORMAT, host, properties.getIdleTimeout().toMillis());
                username = serviceBusConnectionString.getSharedAccessKeyName();
                password = serviceBusConnectionString.getSharedAccessKey();
            }

            if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
                factory = factoryClass.getConstructor(String.class, String.class, String.class)
                                      .newInstance(username, password, remoteUrl);
            } else {
                factory = factoryClass.getConstructor(String.class).newInstance(remoteUrl);
            }

            return factory;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create JmsConnectionFactory", ex);
        }
    }

    private void customize(ServiceBusJmsConnectionFactory connectionFactory) {
        for (ServiceBusJmsConnectionFactoryCustomizer factoryCustomizer : this.factoryCustomizers) {
            factoryCustomizer.customize(connectionFactory);
        }
    }
}
