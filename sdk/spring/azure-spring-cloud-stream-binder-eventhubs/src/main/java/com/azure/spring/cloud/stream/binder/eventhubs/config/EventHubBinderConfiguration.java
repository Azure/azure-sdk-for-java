// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.config;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubOperationAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.EventHubMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubChannelProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubChannelResourceManagerProvisioner;
import com.azure.spring.cloud.stream.binder.eventhubs.provisioning.EventHubProvisioner;
import com.azure.spring.eventhubs.core.EventHubOperation;
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
    AzureEventHubOperationAutoConfiguration.class,
    EventHubBinderHealthIndicatorConfiguration.class
})
@EnableConfigurationProperties(EventHubExtendedBindingProperties.class)
public class EventHubBinderConfiguration {


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EventHubProvisioner.class)
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
                                                       EventHubOperation eventHubOperation,
                                                       EventHubExtendedBindingProperties bindingProperties) {
        EventHubMessageChannelBinder binder =
            new EventHubMessageChannelBinder(null, eventHubChannelProvisioner, eventHubOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }

}
