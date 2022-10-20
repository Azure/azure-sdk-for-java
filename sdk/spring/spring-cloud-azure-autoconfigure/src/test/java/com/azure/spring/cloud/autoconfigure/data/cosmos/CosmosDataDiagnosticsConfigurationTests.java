// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.cosmos.AzureCosmosAutoConfiguration;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CosmosDataDiagnosticsConfigurationTests {

    private static final String ENDPOINT = "https://test.documents.azure.com:443/";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureGlobalPropertiesAutoConfiguration.class,
            AzureCosmosAutoConfiguration.class,
            CosmosDataAutoConfiguration.class));
    CosmosClientBuilder cosmosClientBuilder = mock(CosmosClientBuilder.class);
    CosmosAsyncClient cosmosAsyncClient = mock(CosmosAsyncClient.class);

    @Test
    void configureWithPopulateQueryMetricsEnabled() {
        try (MockedStatic<CosmosFactory> mockedStatic = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            mockedStatic.when(() -> CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder))
                .thenReturn(cosmosAsyncClient);
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> cosmosClientBuilder)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test",
                    "spring.cloud.azure.cosmos.populate-query-metrics=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(CosmosDataDiagnosticsConfiguration.class);
                    assertThat(context).hasSingleBean(ResponseDiagnosticsProcessor.class);
                });
        }
    }

    @Test
    void configureWithPopulateQueryMetricsDisabled() {
        try (MockedStatic<CosmosFactory> mockedStatic = mockStatic(CosmosFactory.class, RETURNS_MOCKS)) {
            mockedStatic.when(() -> CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder))
                .thenReturn(cosmosAsyncClient);
            this.contextRunner
                .withBean(CosmosClientBuilder.class, () -> cosmosClientBuilder)
                .withPropertyValues(
                    "spring.cloud.azure.cosmos.endpoint=" + ENDPOINT,
                    "spring.cloud.azure.cosmos.database=test-database",
                    "spring.cloud.azure.cosmos.populate-query-metrics=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ResponseDiagnosticsProcessor.class);
                    assertThat(context).hasSingleBean(CosmosDataAutoConfiguration.class);
                });
        }
    }

    @Test
    void configureWithUserProvideResponseDiagnosticsProcessor() {
        this.contextRunner
            .withUserConfiguration(ResponseDiagnosticsProcessorConfiguration.class)
            .run(context -> {
                ResponseDiagnosticsProcessor processor = (ResponseDiagnosticsProcessor) context.getBean("ResponseDiagnosticsProcessor");
                assertTrue(processor instanceof ResponseDiagnosticsProcessorExtend);
            });
    }

    @Configuration
    static class ResponseDiagnosticsProcessorConfiguration {
        @Bean(name = "ResponseDiagnosticsProcessor")
        public ResponseDiagnosticsProcessor processor() {
            return new ResponseDiagnosticsProcessorExtend();
        }
    }

    static class ResponseDiagnosticsProcessorExtend implements ResponseDiagnosticsProcessor {
        @Override
        public void processResponseDiagnostics(ResponseDiagnostics responseDiagnostics) {
        }
    }
}
