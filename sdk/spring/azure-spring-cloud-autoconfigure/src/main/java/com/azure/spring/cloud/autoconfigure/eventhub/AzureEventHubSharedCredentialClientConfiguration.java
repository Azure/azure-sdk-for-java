// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.eventhub.factory.EventHubServiceClientBuilderFactory;
import com.azure.spring.cloud.autoconfigure.eventhub.factory.EventProcessorServiceClientBuilderFactory;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.EventHubServiceClientBuilder;
import com.azure.spring.integration.eventhub.factory.EventProcessorServiceClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ EventHubServiceClientBuilder.class, EventProcessorServiceClientBuilder.class })
@Import({
    AzureEventHubSharedCredentialClientConfiguration.EventHubServiceClientConfiguration.class,
    AzureEventHubSharedCredentialClientConfiguration.EventProcessorServiceClientConfiguration.class
})
class AzureEventHubSharedCredentialClientConfiguration {


    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(EventHubServiceClientBuilder.class)
    static class EventHubServiceClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EventHubServiceClientBuilder eventHubClientBuilder(EventHubServiceClientBuilderFactory factory) {
            return factory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubServiceClientBuilderFactory factory(
            AzureEventHubProperties properties,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
            final EventHubServiceClientBuilderFactory builderFactory = new EventHubServiceClientBuilderFactory(properties);

            builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
            return builderFactory;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(EventProcessorServiceClientBuilder.class)
    @ConditionalOnBean(CheckpointStore.class)
    static class EventProcessorServiceClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public EventProcessorServiceClientBuilder evenProcessorClientBuilder(EventProcessorServiceClientBuilderFactory factory) {
            return factory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventProcessorServiceClientBuilderFactory factory(AzureEventHubProperties properties,
                                                                 CheckpointStore checkpointStore) {
            return new EventProcessorServiceClientBuilderFactory(properties, checkpointStore);
        }
    }




}
