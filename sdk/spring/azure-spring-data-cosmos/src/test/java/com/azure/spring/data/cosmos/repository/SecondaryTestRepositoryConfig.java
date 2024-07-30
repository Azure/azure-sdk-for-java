// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * Secondary Database Account
 */
@Configuration
@PropertySource(value = {"classpath:application.properties"})
public class SecondaryTestRepositoryConfig {
    @Value("${cosmos.secondary.uri:}")
    private String cosmosDbUri;

    @Value("${cosmos.secondary.key:}")
    private String cosmosDbKey;

    @Value("${cosmos.secondary.database:}")
    private String database;

    @Value("${cosmos.secondary.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Value("${cosmos.secondary.indexMetricsEnabled}")
    private boolean indexMetricsEnabled;

    @Value("${cosmos.secondary.maxDegreeOfParallelism}")
    private int maxDegreeOfParallelism;

    @Value("${cosmos.secondary.maxBufferedItemCount}")
    private int maxBufferedItemCount;

    @Value("${cosmos.secondary.responseContinuationTokenLimitInKb}")
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
    public CosmosClientBuilder secondaryCosmosClientBuilder() {
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

    @Bean("secondaryCosmosAsyncClient")
    public CosmosAsyncClient getCosmosAsyncClient(CosmosClientBuilder secondaryCosmosClientBuilder) {
        return CosmosFactory.createCosmosAsyncClient(secondaryCosmosClientBuilder);
    }

    /**
     * First database for this account
     */
    @EnableReactiveCosmosRepositories(reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate")
    public class SecondaryDataSourceConfiguration {
        @Bean
        public ReactiveCosmosTemplate secondaryReactiveCosmosTemplate(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, MappingCosmosConverter mappingCosmosConverter) {

            CosmosConfig config =  CosmosConfig.builder()
                .enableQueryMetrics(queryMetricsEnabled)
                .enableIndexMetrics(indexMetricsEnabled)
                .maxDegreeOfParallelism(maxDegreeOfParallelism)
                .maxBufferedItemCount(maxBufferedItemCount)
                .responseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb)
                .build();

            return new ReactiveCosmosTemplate(new CosmosFactory(client, getFirstDatabase()), config, mappingCosmosConverter);
        }
    }

    /**
     * Second database for this account
     */
    @EnableReactiveCosmosRepositories(reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate1")
    public class SecondaryDataSourceConfiguration1 {
        @Bean
        public ReactiveCosmosTemplate secondaryReactiveCosmosTemplate1(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, MappingCosmosConverter mappingCosmosConverter) {

            CosmosConfig config =  CosmosConfig.builder()
                .enableQueryMetrics(queryMetricsEnabled)
                .enableIndexMetrics(indexMetricsEnabled)
                .maxDegreeOfParallelism(maxDegreeOfParallelism)
                .maxBufferedItemCount(maxBufferedItemCount)
                .responseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb)
                .build();

            return new ReactiveCosmosTemplate(new CosmosFactory(client, getSecondDatabase()), config, mappingCosmosConverter);
        }
    }

    private String getFirstDatabase() {
        return StringUtils.hasText(this.database) ? this.database : "test_db_1";
    }

    private String getSecondDatabase() {
        return "test_db_2";
    }

}
