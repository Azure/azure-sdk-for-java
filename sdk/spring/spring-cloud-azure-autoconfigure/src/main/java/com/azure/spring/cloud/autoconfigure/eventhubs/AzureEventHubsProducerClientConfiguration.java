// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.service.implementation.eventhubs.factory.EventHubClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubProducerAsyncClient} and
 * {@link EventHubProducerClient}.
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "event-hub-name", "producer.event-hub-name" })
class AzureEventHubsProducerClientConfiguration {

    @ConditionalOnMissingProperty(prefix = "spring.cloud.azure.eventhubs.producer", name = { "connection-string", "namespace" })
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
    @ConditionalOnBean(EventHubClientBuilder.class)
    @Configuration(proxyBeanMethods = false)
    static class SharedProducerConnectionConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public EventHubProducerAsyncClient eventHubProducerAsyncClient(EventHubClientBuilder builder) {
            return builder.buildAsyncProducerClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubProducerClient eventHubProducerClient(EventHubClientBuilder builder) {
            return builder.buildProducerClient();
        }
    }

    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs.producer", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class DedicatedProducerConnectionConfiguration {

        private final AzureEventHubsProperties.Producer producerProperties;

        DedicatedProducerConnectionConfiguration(AzureEventHubsProperties eventHubsProperties) {
            this.producerProperties = eventHubsProperties.buildProducerProperties();
        }

        @Bean(EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        EventHubClientBuilderFactory eventHubClientBuilderFactoryForProducer(
            ObjectProvider<ServiceConnectionStringProvider<AzureServiceType.EventHubs>> connectionStringProviders,
            ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> customizers) {

            final EventHubClientBuilderFactory factory = new EventHubClientBuilderFactory(this.producerProperties);

            factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS);
            connectionStringProviders.orderedStream().findFirst().ifPresent(factory::setConnectionStringProvider);
            customizers.orderedStream().forEach(factory::addBuilderCustomizer);
            return factory;
        }

        @Bean(EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnBean(name = EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
        EventHubClientBuilder eventHubClientBuilderForProducer(
            @Qualifier(EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME) EventHubClientBuilderFactory clientBuilderFactory) {

            return clientBuilderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubProducerAsyncClient eventHubProducerAsyncClient(@Qualifier(EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
                                                                           EventHubClientBuilder builder) {
            return builder.buildAsyncProducerClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubProducerClient eventHubProducerClient(@Qualifier(EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
                                                                 EventHubClientBuilder builder) {
            return builder.buildProducerClient();
        }

    }

}
