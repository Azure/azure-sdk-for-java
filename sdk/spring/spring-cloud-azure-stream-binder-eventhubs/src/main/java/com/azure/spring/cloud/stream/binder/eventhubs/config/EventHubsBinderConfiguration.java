// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubsResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.provisioning.EventHubsProvisioner;
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
    AzureEventHubsResourceManagerAutoConfiguration.class,
    AzureEventHubsAutoConfiguration.class,
    AzureEventHubsMessagingAutoConfiguration.class,
    EventHubsBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(EventHubsExtendedBindingProperties.class)
public class EventHubsBinderConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ EventHubsProvisioner.class, AzureEventHubsProperties.class })
    public EventHubsChannelProvisioner eventHubChannelArmProvisioner(
        AzureEventHubsProperties eventHubsProperties, EventHubsProvisioner eventHubsProvisioner) {

        return new EventHubsChannelResourceManagerProvisioner(eventHubsProperties.getNamespace(),
                                                             eventHubsProvisioner);
    }

    @Bean
    @ConditionalOnMissingBean({ EventHubsProvisioner.class, EventHubsChannelProvisioner.class })
    public EventHubsChannelProvisioner eventHubChannelProvisioner() {
        return new EventHubsChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubsMessageChannelBinder eventHubBinder(EventHubsChannelProvisioner channelProvisioner,
                                                        EventHubsExtendedBindingProperties bindingProperties,
                                                        ObjectProvider<NamespaceProperties> namespaceProperties,
                                                        CheckpointStore checkpointStore) {
        EventHubsMessageChannelBinder binder = new EventHubsMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        binder.setCheckpointStore(checkpointStore);
        return binder;
    }

}
