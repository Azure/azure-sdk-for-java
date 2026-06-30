// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

class ServiceBusJmsConnectionFactoryFactory {
    private final AzureServiceBusJmsProperties properties;
    private final List<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers;

    ServiceBusJmsConnectionFactoryFactory(AzureServiceBusJmsProperties properties,
                                          List<AzureServiceBusJmsConnectionFactoryCustomizer> factoryCustomizers) {
        Assert.notNull(properties, "Properties must not be null");
        this.properties = properties;
        this.factoryCustomizers = (factoryCustomizers != null) ? factoryCustomizers : Collections.emptyList();
    }

    ServiceBusJmsConnectionFactory createConnectionFactory(AzureServiceBusJmsConnectionFactoryFactory instanceFactory) {
        Assert.notNull(instanceFactory, "AzureServiceBusJmsConnectionFactoryFactory must not be null");
        ServiceBusJmsConnectionFactory factory = createConnectionFactoryInstance(instanceFactory);
        Assert.notNull(
            factory,
            "AzureServiceBusJmsConnectionFactoryFactory must create a non-null ServiceBusJmsConnectionFactory");
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
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.durableTopicPrefetch",
            String.valueOf(prefetchProperties.getDurableTopicPrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.queueBrowserPrefetch",
            String.valueOf(prefetchProperties.getQueueBrowserPrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.queuePrefetch",
            String.valueOf(prefetchProperties.getQueuePrefetch()));
        factory.getSettings().getConfigurationOptions().put("jms.prefetchPolicy.topicPrefetch",
            String.valueOf(prefetchProperties.getTopicPrefetch()));
    }

    private ServiceBusJmsConnectionFactory createConnectionFactoryInstance(AzureServiceBusJmsConnectionFactoryFactory instanceFactory) {
        return instanceFactory.createServiceBusJmsConnectionFactory();
    }

    private void customize(ServiceBusJmsConnectionFactory connectionFactory) {
        for (AzureServiceBusJmsConnectionFactoryCustomizer factoryCustomizer : this.factoryCustomizers) {
            factoryCustomizer.customize(connectionFactory);
        }
    }
}
