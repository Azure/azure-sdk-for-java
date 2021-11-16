// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubsProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import com.azure.spring.core.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.eventhubs.factory.EventHubClientBuilderFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME;

/**
 * Configuration for Event Hub consumer client, which provides {@link EventHubConsumerClient} or
 * {@link EventHubConsumerAsyncClient}.
 *
 */
@Configuration(proxyBeanMethods = false)
@Import({
    AzureEventHubsConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class,
    AzureEventHubsConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class
})
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "event-hub-name", "consumer.event-hub-name" })
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhubs.consumer", name = "consumer-group")
class AzureEventHubsConsumerClientConfiguration {

    @ConditionalOnMissingProperty(prefix = "spring.cloud.azure.eventhubs.consumer", name = { "connection-string", "namespace" })
    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs", name = { "connection-string", "namespace" })
    @ConditionalOnBean(EventHubClientBuilder.class)
    @Configuration(proxyBeanMethods = false)
    static class SharedConsumerConnectionConfiguration {

        private final EventHubClientBuilder builder;
        SharedConsumerConnectionConfiguration(AzureEventHubsProperties properties, EventHubClientBuilder builder) {
            this.builder = builder;

            PropertyMapper mapper = new PropertyMapper();
            mapper.from(properties.getConsumer().getConsumerGroup()).to(builder::consumerGroup);
            mapper.from(properties.getConsumer().getPrefetchCount()).to(builder::prefetchCount);
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubConsumerAsyncClient eventHubConsumerAsyncClient() {
            return this.builder.buildAsyncConsumerClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubConsumerClient eventHubConsumerClient(EventHubClientBuilder builder) {
            return this.builder.buildConsumerClient();
        }
    }

    @ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventhubs.consumer", name = { "connection-string", "namespace" })
    @Configuration(proxyBeanMethods = false)
    static class DedicatedConsumerConnectionConfiguration {

        private final AzureEventHubsProperties.Consumer consumerProperties;

        DedicatedConsumerConnectionConfiguration(AzureEventHubsProperties eventHubProperties) {
            this.consumerProperties = eventHubProperties.buildConsumerProperties();
        }

        @Bean(EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        public EventHubClientBuilderFactory eventHubClientBuilderFactoryForConsumer() {

            final EventHubClientBuilderFactory builderFactory = new EventHubClientBuilderFactory(this.consumerProperties);

            builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUBS,
                                                                                            this.consumerProperties.getConnectionString()));
            builderFactory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_HUB);
            return builderFactory;
        }

        @Bean(EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        @ConditionalOnBean(name = EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
        public EventHubClientBuilder eventHubClientBuilderForConsumer(
            @Qualifier(EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME) EventHubClientBuilderFactory clientBuilderFactory) {

            return clientBuilderFactory.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubConsumerAsyncClient eventHubConsumerAsyncClient(@Qualifier(EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
                                                                                   EventHubClientBuilder builder) {
            return builder.buildAsyncConsumerClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubConsumerClient eventHubConsumerClient(@Qualifier(EVENT_HUB_CONSUMER_CLIENT_BUILDER_BEAN_NAME)
                                                                         EventHubClientBuilder builder) {
            return builder.buildConsumerClient();
        }

    }

}
