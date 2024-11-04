// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos;

import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CosmosDataDiagnosticsConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TestAutoconfigurationClass.class));

    @Test
    void configureWithPopulateQueryMetricsEnabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.cosmos.populate-query-metrics=true")
            .run(context -> {
                assertThat(context).hasSingleBean(CosmosDataDiagnosticsConfiguration.class);
                assertThat(context).hasSingleBean(ResponseDiagnosticsProcessor.class);
            });
    }

    @Test
    void configureWithPopulateQueryMetricsDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.cosmos.populate-query-metrics=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(CosmosDataDiagnosticsConfiguration.class);
                assertThat(context).doesNotHaveBean(ResponseDiagnosticsProcessor.class);
            });
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

    @Configuration
    @Import(CosmosDataDiagnosticsConfiguration.class)
    static class TestAutoconfigurationClass {

    }
}
