// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.core.util.Context;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.mapping.EnableReactiveCosmosAuditing;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
@EnableReactiveCosmosAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableReactiveCosmosRepositories
public class TestRepositoryReactiveAuditingConfig extends AbstractCosmosConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryReactiveAuditingConfig.class);

    @Value("${cosmos.uri:}")
    private String cosmosDbUri;

    @Value("${cosmos.key:}")
    private String cosmosDbKey;

    @Value("${cosmos.database:}")
    private String database;

    @Value("${cosmos.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Value("${cosmos.indexMetricsEnabled}")
    private boolean indexMetricsEnabled;

    @Value("${cosmos.maxDegreeOfParallelism}")
    private int maxDegreeOfParallelism;

    @Value("${cosmos.maxBufferedItemCount}")
    private int maxBufferedItemCount;

    @Value("${cosmos.responseContinuationTokenLimitInKb}")
    private int responseContinuationTokenLimitInKb;

    @Value("${cosmos.diagnosticsThresholds.pointOperationLatencyThresholdInMS}")
    private int pointOperationLatencyThresholdInMS;

    @Bean
    public ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils() {
        return new ResponseDiagnosticsTestUtils();
    }

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        return new CosmosClientBuilder()
            .key(cosmosDbKey)
            .endpoint(cosmosDbUri)
            .contentResponseOnWriteEnabled(true)
            .clientTelemetryConfig(
                new CosmosClientTelemetryConfig()
                    .diagnosticsThresholds(
                        new CosmosDiagnosticsThresholds()
                            .setPointOperationLatencyThreshold(Duration.ofMillis(pointOperationLatencyThresholdInMS))
                    )
                    .diagnosticsHandler(new CosmosDiagnosticsHandler() {
                        @Override
                        public void handleDiagnostics(CosmosDiagnosticsContext ctx, Context traceContext) {
                            logger.info("Diagnostics: {}", ctx);
                        }
                    }));
    }

    @Bean
    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
            .enableQueryMetrics(queryMetricsEnabled)
            .enableIndexMetrics(indexMetricsEnabled)
            .maxDegreeOfParallelism(maxDegreeOfParallelism)
            .maxBufferedItemCount(maxBufferedItemCount)
            .responseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb)
            .responseDiagnosticsProcessor(responseDiagnosticsTestUtils().getResponseDiagnosticsProcessor())
            .build();
    }

    @Override
    protected String getDatabaseName() {
        return StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
    }

    @Bean(name = "auditingDateTimeProvider")
    public StubDateTimeProvider stubDateTimeProvider() {
        return new StubDateTimeProvider();
    }

    @Bean
    public StubReactiveAuditorProvider reactiveAuditorProvider() {
        return new StubReactiveAuditorProvider();
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        final Package mappingBasePackage = getClass().getPackage();
        final String entityPackage = "com.azure.spring.data.cosmos.domain";
        return Arrays.asList(mappingBasePackage.getName(), entityPackage);
    }
}
