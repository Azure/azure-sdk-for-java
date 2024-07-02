// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.actuator.implementation.cosmos.CosmosHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CosmosHealthConfigurationTests {

    private static final String TEST_ENDPOINT = "https://test.https.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.cosmos.endpoint=" + TEST_ENDPOINT)
        .withBean(AzureGlobalProperties.class)
        .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
        .withBean(CosmosAsyncClient.class, () -> mock(CosmosAsyncClient.class))
        .withConfiguration(AutoConfigurations.of(AzureCosmosAutoConfiguration.class, CosmosHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(CosmosHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-cosmos.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(CosmosHealthIndicator.class));
    }
}
