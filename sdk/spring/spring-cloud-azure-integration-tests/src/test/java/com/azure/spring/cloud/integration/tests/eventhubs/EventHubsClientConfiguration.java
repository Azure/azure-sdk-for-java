// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsProducerFactoryCustomizer;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.DefaultEventHubsNamespaceProducerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class EventHubsClientConfiguration {

    @Bean
    AzureServiceClientBuilderCustomizer<EventHubClientBuilder> clientBuilderCustomizer(
        @Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential) {
        return (builder) -> builder.credential(integrationTestTokenCredential);
    }

    @Bean
    AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder> processorClientBuilderCustomizer(
        @Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential) {
        return (builder) -> builder.credential(integrationTestTokenCredential);
    }

    @Bean
    EventHubsProcessorFactoryCustomizer processorFactoryCustomizer(@Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential,
                                                                   AzureTokenCredentialResolver azureTokenCredentialResolver,
                                                                   ObjectProvider<AzureServiceClientBuilderCustomizer<EventProcessorClientBuilder>> processorClientBuilderCustomizers) {
        return (processor -> {
            if (processor instanceof DefaultEventHubsNamespaceProcessorFactory factory) {
                factory.setDefaultCredential(integrationTestTokenCredential);
                factory.setTokenCredentialResolver(azureTokenCredentialResolver);
                processorClientBuilderCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            }
        });
    }

    @Bean
    EventHubsProducerFactoryCustomizer producerFactoryCustomizer(@Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential,
                                                                 AzureTokenCredentialResolver azureTokenCredentialResolver,
                                                                 ObjectProvider<AzureServiceClientBuilderCustomizer<EventHubClientBuilder>> clientBuilderCustomizers) {
        return (producer -> {
            if (producer instanceof DefaultEventHubsNamespaceProducerFactory factory) {
                factory.setDefaultCredential(integrationTestTokenCredential);
                factory.setTokenCredentialResolver(azureTokenCredentialResolver);
                clientBuilderCustomizers.orderedStream().forEach(factory::addBuilderCustomizer);
            }
        });
    }
}
