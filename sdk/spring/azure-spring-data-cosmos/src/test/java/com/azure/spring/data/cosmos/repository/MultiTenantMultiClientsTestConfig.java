// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.config.CosmosConfigurationSupport;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.Duration;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
public class MultiTenantMultiClientsTestConfig extends CosmosConfigurationSupport {
    @Value("${cosmos.uri:}")
    private String primaryCosmosDbUri;

    @Value("${cosmos.key:}")
    private String primaryCosmosDbKey;

    @Value("${cosmos.secondary.uri:}")
    private String secondaryCosmosDbUri;

    @Value("${cosmos.secondary.key:}")
    private String secondaryCosmosDbKey;

    @Value("${cosmos.diagnosticsThresholds.pointOperationLatencyThresholdInMS}")
    private int pointOperationLatencyThresholdInMS;

    @Value("${cosmos.diagnosticsThresholds.nonPointOperationLatencyThresholdInMS}")
    private int nonPointOperationLatencyThresholdInMS;

    @Value("${cosmos.diagnosticsThresholds.requestChargeThresholdInRU}")
    private int requestChargeThresholdInRU;

    @Value("${cosmos.diagnosticsThresholds.payloadSizeThresholdInBytes}")
    private int payloadSizeThresholdInBytes;

    @Value("${cosmos.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Value("${cosmos.maxDegreeOfParallelism}")
    private int maxDegreeOfParallelism;

    @Value("${cosmos.maxBufferedItemCount}")
    private int maxBufferedItemCount;

    @Value("${cosmos.responseContinuationTokenLimitInKb}")
    private int responseContinuationTokenLimitInKb;

    @Qualifier(Constants.OBJECT_MAPPER_BEAN_NAME)
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Bean
    public ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils() {
        return new ResponseDiagnosticsTestUtils();
    }

    @Bean
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
            .enableQueryMetrics(queryMetricsEnabled)
            .maxDegreeOfParallelism(maxDegreeOfParallelism)
            .maxBufferedItemCount(maxBufferedItemCount)
            .responseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb)
            .responseDiagnosticsProcessor(responseDiagnosticsTestUtils().getResponseDiagnosticsProcessor())
            .build();
    }

    @Bean("primaryCosmosAsyncClient")
    public CosmosAsyncClient getPrimaryCosmosAsyncClient() {
        final CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .key(primaryCosmosDbKey)
            .endpoint(primaryCosmosDbUri)
            .contentResponseOnWriteEnabled(true)
            .clientTelemetryConfig(getTelemetryConfig());
        return CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
    }

    @Bean("secondaryCosmosAsyncClient")
    public CosmosAsyncClient getSecondaryCosmosAsyncClient() {
        final CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .key(secondaryCosmosDbKey)
            .endpoint(secondaryCosmosDbUri)
            .contentResponseOnWriteEnabled(true)
            .clientTelemetryConfig(getTelemetryConfig());
        return CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
    }

    private CosmosClientTelemetryConfig getTelemetryConfig() {
        return new CosmosClientTelemetryConfig()
            .diagnosticsThresholds(
                new CosmosDiagnosticsThresholds()
                    .setNonPointOperationLatencyThreshold(Duration.ofMillis(nonPointOperationLatencyThresholdInMS))
                    .setPointOperationLatencyThreshold(Duration.ofMillis(pointOperationLatencyThresholdInMS))
                    .setPayloadSizeThreshold(payloadSizeThresholdInBytes)
                    .setRequestChargeThreshold(requestChargeThresholdInRU)
            )
            .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER);
    }

    @Bean
    public MappingCosmosConverter mappingCosmosConverter(CosmosMappingContext cosmosMappingContext) {
        return new MappingCosmosConverter(cosmosMappingContext, objectMapper);
    }

    @Override
    protected String getDatabaseName() {
        return null;
    }
}
