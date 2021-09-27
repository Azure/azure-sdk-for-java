// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.spring.cloud.autoconfigure.eventhubs.factory.EventHubClientBuilderFactory;
import com.azure.spring.core.ApplicationId;
import com.azure.spring.core.StaticConnectionStringProvider;
import com.azure.spring.core.service.AzureServiceType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    AzureEventHubConsumerClientConfiguration.DedicatedConsumerConnectionConfiguration.class,
    AzureEventHubConsumerClientConfiguration.SharedConsumerConnectionConfiguration.class
})
@ConditionalOnExpression(
    "T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.event-hub-name:}')"
        + " or T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.consumer.event-hub-name:}')"
)
class AzureEventHubConsumerClientConfiguration {

    @ConditionalOnExpression(
        "T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.consumer.connection-string:}')"
            + " and T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.consumer.namespace:}')"
    )
    @Configuration(proxyBeanMethods = false)
    static class SharedConsumerConnectionConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public EventHubConsumerAsyncClient eventHubConsumerAsyncClient(EventHubClientBuilder builder) {
            return builder.buildAsyncConsumerClient();
        }

        @Bean
        @ConditionalOnMissingBean
        public EventHubConsumerClient eventHubConsumerClient(EventHubClientBuilder builder) {
            return builder.buildConsumerClient();
        }
    }

    @ConditionalOnExpression(
        "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.consumer.connection-string:}')"
            + " or !T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.eventhubs.consumer.namespace:}')"
    )
    @Configuration(proxyBeanMethods = false)
    static class DedicatedConsumerConnectionConfiguration {

        private final AzureEventHubProperties.Consumer consumerProperties;

        DedicatedConsumerConnectionConfiguration(AzureEventHubProperties eventHubProperties) {
            this.consumerProperties = eventHubProperties.buildConsumerProperties();
        }

        @Bean(EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        @ConditionalOnMissingBean(name = EVENT_HUB_CONSUMER_CLIENT_BUILDER_FACTORY_BEAN_NAME)
        public EventHubClientBuilderFactory eventHubClientBuilderFactoryForConsumer() {

            final EventHubClientBuilderFactory builderFactory = new EventHubClientBuilderFactory(this.consumerProperties);

            builderFactory.setConnectionStringProvider(new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUB,
                                                                                            this.consumerProperties.getConnectionString()));
            builderFactory.setSpringIdentifier(ApplicationId.AZURE_SPRING_EVENT_HUB);
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
