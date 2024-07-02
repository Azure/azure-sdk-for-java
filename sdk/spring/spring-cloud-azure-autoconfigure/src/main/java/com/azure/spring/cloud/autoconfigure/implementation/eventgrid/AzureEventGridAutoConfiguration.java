// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventgrid;

import com.azure.core.models.CloudEvent;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherAsyncClient;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.AzureServiceConfigurationBase;
import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnAnyProperty;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventgrid.properties.AzureEventGridProperties;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.eventgrid.factory.EventGridPublisherClientBuilderFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnClass(EventGridPublisherClientBuilder.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnAnyProperty(prefix = "spring.cloud.azure.eventgrid", name = "endpoint")
public class AzureEventGridAutoConfiguration extends AzureServiceConfigurationBase {

    protected AzureEventGridAutoConfiguration(AzureGlobalProperties azureProperties) {
        super(azureProperties);
    }

    @Bean
    @ConfigurationProperties(prefix = AzureEventGridProperties.PREFIX)
    AzureEventGridProperties azureEventGridProperties() {
        return loadProperties(getAzureGlobalProperties(), new AzureEventGridProperties());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.event-schema", havingValue = "EVENT_GRID_EVENT", matchIfMissing = true)
    EventGridPublisherClient<EventGridEvent> eventGridEventPublisherClient(EventGridPublisherClientBuilder builder) {
        return builder.buildEventGridEventPublisherClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.event-schema", havingValue = "EVENT_GRID_EVENT", matchIfMissing = true)
    EventGridPublisherAsyncClient<EventGridEvent> eventGridEventPublisherAsyncClient(EventGridPublisherClientBuilder builder) {
        return builder.buildEventGridEventPublisherAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.event-schema", havingValue = "CLOUD_EVENT")
    EventGridPublisherClient<CloudEvent> eventGridCloudEventPublisherClient(EventGridPublisherClientBuilder builder) {
        return builder.buildCloudEventPublisherClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.event-schema", havingValue = "CLOUD_EVENT")
    EventGridPublisherAsyncClient<CloudEvent> eventGridCloudEventPublisherAsyncClient(EventGridPublisherClientBuilder builder) {
        return builder.buildCloudEventPublisherAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.event-schema", havingValue = "CUSTOM_EVENT")
    EventGridPublisherClient<BinaryData> eventGridCustomEventPublisherClient(EventGridPublisherClientBuilder builder) {
        return builder.buildCustomEventPublisherClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.azure.eventgrid.event-schema", havingValue = "CUSTOM_EVENT")
    EventGridPublisherAsyncClient<BinaryData> eventGridCustomEventPublisherAsyncClient(EventGridPublisherClientBuilder builder) {
        return builder.buildCustomEventPublisherAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    EventGridPublisherClientBuilder eventGridPublisherClientBuilder(EventGridPublisherClientBuilderFactory factory) {
        return factory.build();
    }

    @Bean
    @ConditionalOnMissingBean
    EventGridPublisherClientBuilderFactory eventGridPublisherClientBuilderFactory(
        AzureEventGridProperties properties,
        ObjectProvider<AzureServiceClientBuilderCustomizer<EventGridPublisherClientBuilder>> customizers) {
        EventGridPublisherClientBuilderFactory factory = new EventGridPublisherClientBuilderFactory(properties);

        factory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_EVENT_GRID);
        customizers.orderedStream().forEach(factory::addBuilderCustomizer);
        return factory;
    }
}
