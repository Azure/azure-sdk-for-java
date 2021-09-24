// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubSharedAuthenticationClientBuilderFactory;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventProcessorSharedAuthenticationClientBuilderFactory;
import com.azure.spring.core.ConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.EventHubSharedAuthenticationClientBuilder;
import com.azure.spring.integration.eventhub.factory.EventProcessorSharedAuthenticationClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ EventHubSharedAuthenticationClientBuilder.class, EventProcessorSharedAuthenticationClientBuilder.class })
@Import({
    AzureEventHubSharedCredentialClientConfiguration.EventHubServiceClientConfiguration.class,
    AzureEventHubSharedCredentialClientConfiguration.EventProcessorServiceClientConfiguration.class
})
class AzureEventHubSharedCredentialClientConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(EventHubSharedAuthenticationClientBuilder.class)
    static class EventHubServiceClientConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EventHubSharedAuthenticationClientBuilder eventHubSharedAuthenticationClientBuilder(
            EventHubSharedAuthenticationClientBuilderFactory factory) {
            return factory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubSharedAuthenticationClientBuilderFactory eventHubServiceClientBuilderFactory(
            AzureEventHubProperties properties,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
            final EventHubSharedAuthenticationClientBuilderFactory builderFactory = new EventHubSharedAuthenticationClientBuilderFactory(properties);

            builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
            return builderFactory;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(EventProcessorSharedAuthenticationClientBuilder.class)
    @ConditionalOnBean(CheckpointStore.class)
    static class EventProcessorServiceClientConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public EventProcessorSharedAuthenticationClientBuilder eventProcessorSharedAuthenticationClientBuilder(
            EventProcessorSharedAuthenticationClientBuilderFactory factory) {
            return factory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventProcessorSharedAuthenticationClientBuilderFactory eventProcessorSharedAuthenticationClientBuilderFactory(
            AzureEventHubProperties properties,
            CheckpointStore checkpointStore,
            ObjectProvider<ConnectionStringProvider<AzureServiceType.EventHub>> connectionStringProviders) {
            final EventProcessorSharedAuthenticationClientBuilderFactory builderFactory = new EventProcessorSharedAuthenticationClientBuilderFactory(
                properties, checkpointStore);
            builderFactory.setConnectionStringProvider(connectionStringProviders.getIfAvailable());
            return builderFactory;
        }
    }

}
