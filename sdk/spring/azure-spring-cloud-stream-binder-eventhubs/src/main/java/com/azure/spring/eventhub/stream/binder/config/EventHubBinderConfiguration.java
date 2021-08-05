// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.config;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.eventhub.EventHubUtils;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.EventHubConsumerGroupManager;
import com.azure.spring.cloud.context.core.impl.EventHubManager;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.eventhub.stream.binder.EventHubMessageChannelBinder;
import com.azure.spring.eventhub.stream.binder.properties.EventHubExtendedBindingProperties;
import com.azure.spring.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import com.azure.spring.eventhub.stream.binder.provisioning.EventHubChannelResourceManagerProvisioner;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureEnvironmentAutoConfiguration.class,
    AzureContextAutoConfiguration.class,
    AzureEventHubAutoConfiguration.class,
    EventHubBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties({ AzureEventHubProperties.class, EventHubExtendedBindingProperties.class })
public class EventHubBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventHubNamespaceManager.class)
    public EventHubManager eventHubManager(AzureResourceManager azureResourceManager, AzureProperties azureProperties) {
        return new EventHubManager(azureResourceManager, azureProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventHubNamespaceManager.class)
    public EventHubConsumerGroupManager eventHubConsumerGroupManager(AzureResourceManager azureResourceManager,
                                                                     AzureProperties azureProperties) {
        return new EventHubConsumerGroupManager(azureResourceManager, azureProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventHubConnectionStringProvider.class)
    public EventHubChannelProvisioner eventHubChannelProvisioner(
        EventHubConnectionStringProvider eventHubConnectionStringProvider,
        AzureEventHubProperties eventHubProperties,
        @Autowired(required = false) EventHubNamespaceManager eventHubNamespaceManager,
        @Autowired(required = false) EventHubManager eventHubManager,
        @Autowired(required = false) EventHubConsumerGroupManager consumerGroupManager) {

        final String connectionString = eventHubConnectionStringProvider.getConnectionString();
        String namespace = eventHubProperties.getNamespace();

        if (namespace == null) {
            namespace = EventHubUtils.getNamespace(connectionString);
        }

        if (consumerGroupManager != null) {
            return new EventHubChannelResourceManagerProvisioner(eventHubNamespaceManager,
                eventHubManager,
                consumerGroupManager,
                namespace);
        }


        // TODO: With the previous ResourceManagerProvider architecture, eventHubManager
        // and eventHubConsumerGroup manager were created unconditionally.
        // Now, they are not created at all. Should they be?

        return new EventHubChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventHubConnectionStringProvider.class)
    public EventHubMessageChannelBinder eventHubBinder(EventHubChannelProvisioner eventHubChannelProvisioner,
                                                       EventHubOperation eventHubOperation,
                                                       EventHubExtendedBindingProperties bindingProperties) {
        EventHubMessageChannelBinder binder =
            new EventHubMessageChannelBinder(null, eventHubChannelProvisioner, eventHubOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }

}
