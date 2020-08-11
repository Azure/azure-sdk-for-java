// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhub.stream.binder.config;

import com.microsoft.azure.eventhub.stream.binder.EventHubMessageChannelBinder;
import com.microsoft.azure.eventhub.stream.binder.properties.EventHubExtendedBindingProperties;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubChannelProvisioner;
import com.microsoft.azure.eventhub.stream.binder.provisioning.EventHubChannelResourceManagerProvisioner;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.eventhub.EventHubUtils;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @author Warren Zhu
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import({AzureEventHubAutoConfiguration.class, AzureEnvironmentAutoConfiguration.class})
@EnableConfigurationProperties({AzureEventHubProperties.class, EventHubExtendedBindingProperties.class})
public class EventHubBinderConfiguration {

    private static final String EVENT_HUB_BINDER = "EventHubBinder";
    private static final String NAMESPACE = "Namespace";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(EVENT_HUB_BINDER);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubChannelProvisioner eventHubChannelProvisioner(AzureEventHubProperties eventHubProperties) {
        if (resourceManagerProvider != null) {
            return new EventHubChannelResourceManagerProvisioner(resourceManagerProvider,
                    eventHubProperties.getNamespace());
        } else {
            TelemetryCollector.getInstance().addProperty(EVENT_HUB_BINDER, NAMESPACE,
                    EventHubUtils.getNamespace(eventHubProperties.getConnectionString()));
        }

        return new EventHubChannelProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubMessageChannelBinder eventHubBinder(EventHubChannelProvisioner eventHubChannelProvisioner,
            EventHubOperation eventHubOperation, EventHubExtendedBindingProperties bindingProperties) {
        EventHubMessageChannelBinder binder =
                new EventHubMessageChannelBinder(null, eventHubChannelProvisioner, eventHubOperation);
        binder.setBindingProperties(bindingProperties);
        return binder;
    }
}
