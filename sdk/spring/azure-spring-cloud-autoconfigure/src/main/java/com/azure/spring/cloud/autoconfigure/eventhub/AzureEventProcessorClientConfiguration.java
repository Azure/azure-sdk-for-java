// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhub.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.integration.eventhub.api.EventProcessorListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures a {@link EventProcessorClient}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventProcessorClientBuilder.class)
@ConditionalOnBean({ EventProcessorListener.class, CheckpointStore.class })
class AzureEventProcessorClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClient eventProcessorClient(EventProcessorClientBuilder builder) {
        return builder.buildEventProcessorClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClientBuilderFactory factory(AzureEventHubProperties properties,
                                                      CheckpointStore checkpointStore,
                                                      EventProcessorListener listener) {
        return new EventProcessorClientBuilderFactory(properties, checkpointStore, listener);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClientBuilder evenProcessorClientBuilder(EventProcessorClientBuilderFactory factory) {
        return factory.build();
    }
}
