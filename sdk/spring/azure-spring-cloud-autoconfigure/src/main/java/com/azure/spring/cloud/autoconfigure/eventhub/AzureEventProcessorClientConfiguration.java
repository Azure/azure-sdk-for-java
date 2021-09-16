// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhub.factory.EventProcessorClientBuilderFactory;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.eventhub.api.EventProcessorListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhub.eventhub-name:}')")
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
                                                      EventProcessorListener listener,
                                                      ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
        final EventProcessorClientBuilderFactory factory = new EventProcessorClientBuilderFactory(properties,
                                                                                                  checkpointStore,
                                                                                                  listener);
        factory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public EventProcessorClientBuilder evenProcessorClientBuilder(EventProcessorClientBuilderFactory factory) {
        return factory.build();
    }
}
