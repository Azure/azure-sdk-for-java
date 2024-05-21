// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsHandler;
import com.azure.cosmos.CosmosDiagnosticsThresholds;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.time.Duration;

@Configuration
@EnableCosmosRepositories
public class AppConfigurationCodeSnippet extends AbstractCosmosConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationCodeSnippet.class);

    // configuration code
    @Value("${azure.cosmos.uri}")
    private String uri;

    @Value("${azure.cosmos.key}")
    private String key;

    @Value("${azure.cosmos.secondaryKey}")
    private String secondaryKey;

    @Value("${azure.cosmos.database}")
    private String dbName;

    @Value("${azure.cosmos.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    @Value("${azure.cosmos.indexMetricsEnabled}")
    private boolean indexMetricsEnabled;

    @Value("${azure.cosmos.maxDegreeOfParallelism}")
    private int maxDegreeOfParallelism;

    @Value("${azure.cosmos.maxBufferedItemCount}")
    private int maxBufferedItemCount;

    @Value("${azure.cosmos.responseContinuationTokenLimitInKb}")
    private int responseContinuationTokenLimitInKb;

    @Value("${azure.cosmos.diagnosticsThresholds.pointOperationLatencyThresholdInMS}")
    private int pointOperationLatencyThresholdInMS;

    @Value("${azure.cosmos.diagnosticsThresholds.nonPointOperationLatencyThresholdInMS}")
    private int nonPointOperationLatencyThresholdInMS;

    @Value("${azure.cosmos.diagnosticsThresholds.requestChargeThresholdInRU}")
    private int requestChargeThresholdInRU;

    @Value("${azure.cosmos.diagnosticsThresholds.payloadSizeThresholdInBytes}")
    private int payloadSizeThresholdInBytes;

    // BEGIN: readme-sample-AppConfigurationCodeSnippet
    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {

        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        return new CosmosClientBuilder()
            .endpoint(uri)
            .directMode(directConnectionConfig, gatewayConnectionConfig)
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

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(queryMetricsEnabled)
                           .enableIndexMetrics(indexMetricsEnabled)
                           .maxDegreeOfParallelism(maxDegreeOfParallelism)
                           .maxBufferedItemCount(maxBufferedItemCount)
                           .responseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb)
                           .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                           .build();
    }
    // END: readme-sample-AppConfigurationCodeSnippet

    @Override
    protected String getDatabaseName() {
        return dbName;
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            LOGGER.info("Response Diagnostics {}", responseDiagnostics);
        }
    }
}
