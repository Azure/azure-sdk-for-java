// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.eventhubs.core.DefaultEventHubClientFactory;
import com.azure.spring.eventhubs.core.EventHubClientFactory;
import com.azure.spring.eventhubs.core.EventHubOperation;
import com.azure.spring.eventhubs.core.EventHubSharedAuthenticationClientBuilder;
import com.azure.spring.eventhubs.core.EventHubTemplate;
import com.azure.spring.eventhubs.core.EventProcessorSharedAuthenticationClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubOperation.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
@AutoConfigureAfter(AzureEventHubAutoConfiguration.class)
@Import(AzureEventHubSharedCredentialClientConfiguration.class)
public class AzureEventHubOperationAutoConfiguration {

    // TODO (xiada): should processor be optional?
    @Bean
    @ConditionalOnMissingBean
    public EventHubClientFactory eventhubClientFactory(EventHubSharedAuthenticationClientBuilder eventHubServiceClientBuilder,
                                                       ObjectProvider<EventProcessorSharedAuthenticationClientBuilder> eventProcessorBuilders) {
        DefaultEventHubClientFactory factory = new DefaultEventHubClientFactory(eventHubServiceClientBuilder);

        if (eventProcessorBuilders.getIfAvailable() != null) {
            factory.setEventProcessorServiceClientBuilder(eventProcessorBuilders.getIfAvailable());
        }

        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubOperation eventHubOperation(EventHubClientFactory clientFactory) {
        return new EventHubTemplate(clientFactory);
    }

}
