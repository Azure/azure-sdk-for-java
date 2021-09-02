// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * An auto-configuration for Event Hub, which provides {@link EventHubOperation}
 *
 * @author Warren Zhu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EventHubClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.eventhub", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureEventHubProperties.class)
@AutoConfigureAfter
public class AzureEventHubClientAutoConfiguration {

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

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientBuilderFactory factory(AzureEventHubProperties properties) {
        return new EventHubClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventHubClientBuilder eventHubClientBuilder(EventHubClientBuilderFactory factory) {
        return factory.build();
    }
}
