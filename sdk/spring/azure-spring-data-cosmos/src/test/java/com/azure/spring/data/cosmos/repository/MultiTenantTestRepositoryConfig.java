// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.MultiTenantDBCosmosFactory;
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
@EnableReactiveCosmosRepositories
public class MultiTenantTestRepositoryConfig extends AbstractCosmosConfiguration {
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
                            .setNonPointOperationLatencyThreshold(Duration.ofMillis(nonPointOperationLatencyThresholdInMS))
                            .setPointOperationLatencyThreshold(Duration.ofMillis(pointOperationLatencyThresholdInMS))
                            .setPayloadSizeThreshold(payloadSizeThresholdInBytes)
                            .setRequestChargeThreshold(requestChargeThresholdInRU)
                    )
                    .diagnosticsHandler(CosmosDiagnosticsHandler.DEFAULT_LOGGING_HANDLER));
    }

    @Bean
    public MultiTenantDBCosmosFactory cosmosFactory(CosmosAsyncClient cosmosAsyncClient) {
        return new MultiTenantDBCosmosFactory(cosmosAsyncClient, getDatabaseName());
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
