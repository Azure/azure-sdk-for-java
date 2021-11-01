// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubChannelResourceManagerProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubProvisioner;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({
    AzureGlobalPropertiesAutoConfiguration.class,
    AzureResourceManagerAutoConfiguration.class,
    AzureEventHubResourceManagerAutoConfiguration.class,
    AzureEventHubAutoConfiguration.class,
    AzureEventHubMessagingAutoConfiguration.class,
    EventHubBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(EventHubExtendedBindingProperties.class)
public class EventHubBinderConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ EventHubProvisioner.class, AzureEventHubProperties.class })
    public EventHubChannelProvisioner eventHubChannelArmProvisioner(AzureEventHubProperties eventHubProperties,
                                                                    EventHubProvisioner eventHubProvisioner) {

        return new EventHubChannelResourceManagerProvisioner(eventHubProperties.getNamespace(),
                                                             eventHubProvisioner);
    }

    @Bean
    @ConditionalOnMissingBean({ EventHubProvisioner.class, EventHubChannelProvisioner.class })
    public EventHubChannelProvisioner eventHubChannelProvisioner() {
        return new EventHubChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubMessageChannelBinder eventHubBinder(EventHubChannelProvisioner eventHubChannelProvisioner,
                                                       EventHubExtendedBindingProperties bindingProperties,
                                                       ObjectProvider<NamespaceProperties> namespaceProperties,
                                                       CheckpointStore checkpointStore) {
        EventHubMessageChannelBinder binder = new EventHubMessageChannelBinder(null, eventHubChannelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        binder.setCheckpointStore(checkpointStore);
        return binder;
    }

}
