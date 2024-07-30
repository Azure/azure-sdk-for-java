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
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import com.azure.spring.data.cosmos.core.mapping.event.SimpleCosmosMappingEventListener;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

    @Value("${cosmos.diagnosticsThresholds.nonPointOperationLatencyThresholdInMS}")
    private int nonPointOperationLatencyThresholdInMS;

    @Value("${cosmos.diagnosticsThresholds.requestChargeThresholdInRU}")
    private int requestChargeThresholdInRU;

    @Value("${cosmos.diagnosticsThresholds.payloadSizeThresholdInBytes}")
    private int payloadSizeThresholdInBytes;

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryConfig.class);

    public static final CapturingLogger capturingLogger = new CapturingLogger();

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
                        .setNonPointOperationLatencyThreshold(Duration.ofMillis(10))
                        .setPointOperationLatencyThreshold(Duration.ofMillis(pointOperationLatencyThresholdInMS))
                        .setPayloadSizeThreshold(payloadSizeThresholdInBytes)
                        .setRequestChargeThreshold(requestChargeThresholdInRU)
                )
                .diagnosticsHandler(capturingLogger));
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

    public static class CapturingLogger implements CosmosDiagnosticsHandler {
        public List<String> loggedMessages = new ArrayList<>();
        public CapturingLogger() {
            super();
        }

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext ctx, Context traceContext) {
            logger.info("--> log - ctx: {}", ctx);
            String msg = String.format(
                "Account: %s -> DB: %s, Col:%s, StatusCode: %d:%d Diagnostics: %s",
                ctx.getAccountName(),
                ctx.getDatabaseName(),
                ctx.getContainerName(),
                ctx.getStatusCode(),
                ctx.getSubStatusCode(),
                ctx);

            this.loggedMessages.add(msg);

            logger.info(msg);
        }

        public List<String> getLoggedMessages() {
            return this.loggedMessages;
        }
    }
}
