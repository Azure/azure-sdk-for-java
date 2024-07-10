// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class CosmosDataAutoConfigurationTests {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CosmosDataAutoConfiguration.class));

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
    @SuppressWarnings("try")
    void cosmosTemplateExistsAndUsesIt() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            AzureCosmosProperties azureCosmosProperties = new AzureCosmosProperties();
            azureCosmosProperties.setEndpoint(ENDPOINT);
            azureCosmosProperties.setDatabase("test");

            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureCosmosProperties.class, () -> azureCosmosProperties)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                    assertThat(context).hasSingleBean(ReactiveCosmosTemplate.class);
                });
        }
    }

    @Test
    @SuppressWarnings("try")
    void noResponseDiagnosticsProcessorByDefault() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            AzureCosmosProperties azureCosmosProperties = new AzureCosmosProperties();
            azureCosmosProperties.setEndpoint(ENDPOINT);
            azureCosmosProperties.setDatabase("test");

            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureCosmosProperties.class, () -> azureCosmosProperties)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ResponseDiagnosticsProcessor.class);
                    assertThat(context).doesNotHaveBean(CosmosDataDiagnosticsConfiguration.class);
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                });
        }
    }

    @Test
    @SuppressWarnings("try")
    void hasResponseDiagnosticsProcessorWhenEnableQueryMetrics() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            AzureCosmosProperties azureCosmosProperties = new AzureCosmosProperties();
            azureCosmosProperties.setEndpoint(ENDPOINT);
            azureCosmosProperties.setDatabase("test");
            azureCosmosProperties.setPopulateQueryMetrics(true);

            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureCosmosProperties.class, () -> azureCosmosProperties)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test",
                    "spring.cloud.azure.cosmos.populate-query-metrics=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(ResponseDiagnosticsProcessor.class);
                    assertThat(context).hasSingleBean(CosmosDataDiagnosticsConfiguration.class);
                    assertThat(context).hasSingleBean(CosmosConfig.class);

                    ResponseDiagnosticsProcessor responseDiagnosticsProcessor = context.getBean(ResponseDiagnosticsProcessor.class);
                    CosmosConfig bean = context.getBean(CosmosConfig.class);
                    assertThat(bean.getResponseDiagnosticsProcessor()).isEqualTo(responseDiagnosticsProcessor);
                });
        }
    }

    @SuppressWarnings("try")
    @Test
    void testWithEnableAuditing() {
        try (MockedStatic<CosmosFactory> ignored = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            AzureCosmosProperties azureCosmosProperties = new AzureCosmosProperties();
            azureCosmosProperties.setEndpoint(ENDPOINT);
            azureCosmosProperties.setDatabase("test");

            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> mock(CosmosClientBuilder.class))
                .withBean(AzureCosmosProperties.class, () -> azureCosmosProperties)
                .withUserConfiguration(UserAuditingConfiguration.class)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosConfig.class);
                    assertThat(context).hasSingleBean(CosmosTemplate.class);
                });
        }
    }

    @EnableCosmosAuditing
    static class UserAuditingConfiguration {

    }
}
