// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.RETURNS_MOCKS;

class CosmosDataAutoConfigurationTests {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureCosmosAutoConfiguration.class,
            CosmosDataAutoConfiguration.class));

    @Test
    void configureWithoutCosmosTemplate() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(CosmosTemplate.class))
            .run((context) -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void configureWithoutEndpoint() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void cosmosTemplateExistsAndUsesIt() {
        try (MockedStatic<CosmosFactory> mockedStatic = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            CosmosClientBuilder cosmosClientBuilder = mock(CosmosClientBuilder.class);
            CosmosAsyncClient cosmosAsyncClient = mock(CosmosAsyncClient.class);
            when(cosmosClientBuilder.buildAsyncClient()).thenReturn(cosmosAsyncClient);
            mockedStatic.when(() -> CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder))
                .thenReturn(cosmosAsyncClient);
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> cosmosClientBuilder)
                .withBean(CosmosClient.class, () -> mock(CosmosClient.class))
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test")
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                    assertThat(context).hasSingleBean(ReactiveCosmosTemplate.class);
                });
        }
    }
}
