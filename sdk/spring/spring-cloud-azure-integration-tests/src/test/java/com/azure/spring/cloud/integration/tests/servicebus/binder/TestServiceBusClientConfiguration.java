// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.servicebus.binder;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProducerFactoryCustomizer;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProcessorFactory;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestServiceBusClientConfiguration {

    @Bean
    ServiceBusProcessorFactoryCustomizer processorFactoryCustomizer(@Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential,
                                                                    AzureTokenCredentialResolver azureTokenCredentialResolver,
                                                                    ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers) {
        return (processor -> {
            if (processor instanceof DefaultServiceBusNamespaceProcessorFactory factory) {
                factory.setDefaultCredential(integrationTestTokenCredential);
                factory.setTokenCredentialResolver(azureTokenCredentialResolver);
                clientBuilderCustomizers.orderedStream().forEach(factory::addServiceBusClientBuilderCustomizer);
                factory.addServiceBusClientBuilderCustomizer(builder -> builder.credential(integrationTestTokenCredential));
            }
        });
    }

    @Bean
    ServiceBusProducerFactoryCustomizer producerFactoryCustomizer(@Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential,
                                                                  AzureTokenCredentialResolver azureTokenCredentialResolver,
                                                                  ObjectProvider<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers) {
        return (producer -> {
            if (producer instanceof DefaultServiceBusNamespaceProducerFactory factory) {
                factory.setDefaultCredential(integrationTestTokenCredential);
                factory.setTokenCredentialResolver(azureTokenCredentialResolver);
                clientBuilderCustomizers.orderedStream().forEach(factory::addServiceBusClientBuilderCustomizer);
                factory.addServiceBusClientBuilderCustomizer(builder -> builder.credential(integrationTestTokenCredential));
            }
        });
    }
}
