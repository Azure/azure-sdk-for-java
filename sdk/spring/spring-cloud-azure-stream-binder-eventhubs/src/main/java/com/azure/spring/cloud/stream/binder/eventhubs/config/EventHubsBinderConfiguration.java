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
import com.azure.spring.resourcemanager.provisioning.eventhubs.EventHubsProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubsMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubsChannelResourceManagerProvisioner;
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
@Configuration(proxyBeanMethods = false)
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


    /**
     * Declare Event Hubs Channel Provisioner bean.
     *
     * @param eventHubsProperties the event Hubs Properties
     * @param eventHubsProvisioner the event Hubs Provisioner
     * @return EventHubsChannelProvisioner bean the Event Hubs Channel Provisioner bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ EventHubsProvisioner.class, AzureEventHubsProperties.class })
    public EventHubsChannelProvisioner eventHubChannelArmProvisioner(
        AzureEventHubsProperties eventHubsProperties, EventHubsProvisioner eventHubsProvisioner) {

        return new EventHubsChannelResourceManagerProvisioner(eventHubsProperties.getNamespace(),
                                                             eventHubsProvisioner);
    }

    /**
     * Declare Event Hubs Channel Provisioner bean.
     *
     * @return EventHubsChannelProvisioner bean the Event Hubs Channel Provisioner bean
     */
    @Bean
    @ConditionalOnMissingBean({ EventHubsProvisioner.class, EventHubsChannelProvisioner.class })
    public EventHubsChannelProvisioner eventHubChannelProvisioner() {
        return new EventHubsChannelProvisioner();
    }

    /**
     * Declare Event Hubs Message Channel Binder bean.
     *
     * @param channelProvisioner the channel Provisioner
     * @param bindingProperties the binding Properties
     * @param namespaceProperties the namespace Properties
     * @param checkpointStores the checkpoint Stores
     * @return EventHubsMessageChannelBinder bean the Event Hubs Message Channel Binder bean
     */
    @Bean
    @ConditionalOnMissingBean
    public EventHubsMessageChannelBinder eventHubBinder(EventHubsChannelProvisioner channelProvisioner,
                                                        EventHubsExtendedBindingProperties bindingProperties,
                                                        ObjectProvider<NamespaceProperties> namespaceProperties,
                                                        ObjectProvider<CheckpointStore> checkpointStores) {
        EventHubsMessageChannelBinder binder = new EventHubsMessageChannelBinder(null, channelProvisioner);
        binder.setBindingProperties(bindingProperties);
        binder.setNamespaceProperties(namespaceProperties.getIfAvailable());
        checkpointStores.ifAvailable(binder::setCheckpointStore);
        return binder;
    }

}
