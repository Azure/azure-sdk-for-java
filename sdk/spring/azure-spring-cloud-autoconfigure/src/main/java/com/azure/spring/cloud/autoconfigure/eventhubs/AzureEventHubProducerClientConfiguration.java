// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubClientBuilderFactory;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.eventhubs.core.EventHubOperation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "event-hub-name", "producer.event-hub-name" })
class AzureEventHubProducerClientConfiguration {

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

        private final AzureEventHubProperties.Producer producerProperties;

        DedicatedProducerConnectionConfiguration(AzureEventHubProperties eventHubProperties) {
            this.producerProperties = eventHubProperties.buildProducerProperties();
        }

        @Bean(EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        public EventHubClientBuilderFactory eventHubClientBuilderFactoryForProducer() {

            final EventHubClientBuilderFactory builderFactory = new EventHubClientBuilderFactory(this.producerProperties);

            builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                                                            this.producerProperties.getConnectionString()));
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_EVENT_HUB);
            return builderFactory;
        }

        @Bean(EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnBean(name = EVENT_HUB_PRODUCER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_PRODUCER_CLIENT_BUILDER_BEAN_NAME)
        public EventHubClientBuilder eventHubClientBuilderForProducer(
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
