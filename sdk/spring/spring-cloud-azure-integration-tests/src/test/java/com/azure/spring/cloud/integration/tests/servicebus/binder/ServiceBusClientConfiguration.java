// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.integration.tests.servicebus.binder;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ServiceBusClientConfiguration {

    @Bean
    AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder> clientBuilderCustomizer(
        @Qualifier("integrationTestTokenCredential") TokenCredential integrationTestTokenCredential) {
        return (builder) -> builder.credential(integrationTestTokenCredential);
    }
}
