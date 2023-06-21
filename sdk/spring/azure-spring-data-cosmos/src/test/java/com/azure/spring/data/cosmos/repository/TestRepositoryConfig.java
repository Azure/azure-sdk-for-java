// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import com.azure.spring.data.cosmos.core.mapping.event.SimpleCosmosMappingEventListener;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

@Configuration
@PropertySource(value = { "classpath:application.properties" })
@EnableCosmosRepositories
@EnableCosmosAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@EnableReactiveCosmosRepositories
public class TestRepositoryConfig extends AbstractCosmosConfiguration {
    @Value("${cosmos.uri:}")
    private String cosmosDbUri;

    @Value("${cosmos.key:}")
    private String cosmosDbKey;

    @Value("${cosmos.database:}")
    private String database;

    @Value("${cosmos.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Value("${cosmos.maxDegreeOfParallelism}")
    private int maxDegreeOfParallelism;

    @Value("${cosmos.maxBufferedItemCount}")
    private int maxBufferedItemCount;

    @Value("${cosmos.responseContinuationTokenLimitInKb}")
    private int responseContinuationTokenLimitInKb;

    @Value("${cosmos.diagnosticsThresholds.pointOperationLatencyThreshold}")
    private int pointOperationLatencyThreshold;

    @Value("${cosmos.diagnosticsThresholds.nonPointOperationLatencyThreshold}")
    private int nonPointOperationLatencyThreshold;

    @Value("${cosmos.diagnosticsThresholds.requestChargeThreshold}")
    private int requestChargeThreshold;

    @Value("${cosmos.diagnosticsThresholds.payloadSizeInBytesThreshold}")
    private int payloadSizeInBytesThreshold;

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
                        .setNonPointOperationLatencyThreshold(Duration.ofSeconds(nonPointOperationLatencyThreshold))
                        .setPointOperationLatencyThreshold(Duration.ofSeconds(pointOperationLatencyThreshold))
                        .setPayloadSizeThreshold(payloadSizeInBytesThreshold)
                        .setRequestChargeThreshold(requestChargeThreshold)
                )
                .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER));
    }

    @Bean
    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(queryMetricsEnabled)
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
    public StubAuditorProvider auditorProvider() {
        return new StubAuditorProvider();
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        final Package mappingBasePackage = getClass().getPackage();
        final String entityPackage = "com.azure.spring.data.cosmos.domain";
        return Arrays.asList(mappingBasePackage.getName(), entityPackage);
    }

    @Bean
    SimpleCosmosMappingEventListener simpleMappingEventListener() {
        return new SimpleCosmosMappingEventListener();
    }
}
