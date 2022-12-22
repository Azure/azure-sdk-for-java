// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosDiagnosticsContext;
import com.azure.cosmos.models.CosmosDiagnosticsLoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosClientBuilderTest {

    private final static Logger logger = LoggerFactory.getLogger(CosmosClientBuilderTest.class);

    String hostName = "https://sample-account.documents.azure.com:443/";

    @Test(groups = "unit")
    public void validateBadPreferredRegions1() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(Arrays.asList("westus1,eastus1"))
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e).hasCauseExactlyInstanceOf(URISyntaxException.class);
            assertThat(e.getMessage()).isEqualTo("invalid location [westus1,eastus1] or serviceEndpoint [https://sample-account.documents.azure.com:443/]");
        }
    }

    @Test(groups = "unit")
    public void validateBadPreferredRegions2() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(Arrays.asList(" "))
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("preferredRegion can't be empty");
        }
    }

    @Test(groups = "emulator")
    public void validateIsClientTelemetryEnabledConflicts() {
        ImplementationBridgeHelpers.CosmosClientTelemetryConfigHelper.CosmosClientTelemetryConfigAccessor
            telemetryAccessor = ImplementationBridgeHelpers
                .CosmosClientTelemetryConfigHelper
                .getCosmosClientTelemetryConfigAccessor();

        // DEFAULT - clientTelemetry should be disabled for now
        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig();

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(false);
        client.close();

        // Enabling via telemetryConfig
        telemetryConfig.sendClientTelemetryToService(true);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(true);
        client.close();

        // Enabling via telemetryConfig and disabled via builder.isClientTelemetryEnabled --> Disabled
        telemetryConfig.sendClientTelemetryToService(true);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .clientTelemetryEnabled(false)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(false);
        client.close();

        // Enabling via telemetryConfig, disabling via builder.isClientTelemetryEnabled and re-enabling again --> Enabled
        telemetryConfig.sendClientTelemetryToService(true);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .clientTelemetryEnabled(false)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(true);
        client.close();

        // Disabling via telemetryConfig, enabling via builder.isClientTelemetryEnabled --> Enabled
        telemetryConfig.sendClientTelemetryToService(false);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .clientTelemetryEnabled(true)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(true);
        client.close();

        // Disabling via telemetryConfig, enabling via builder.isClientTelemetryEnabled and
        // re-disabling again --> Disabled
        telemetryConfig.sendClientTelemetryToService(false);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .clientTelemetryEnabled(true)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(false);
        client.close();

        // Enabling via telemetryConfig, enabling via builder.isClientTelemetryEnabled --> Enabled
        telemetryConfig.sendClientTelemetryToService(true);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryEnabled(true)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(true);
        client.close();

        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .clientTelemetryEnabled(true)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(true);
        client.close();
    }

    @Test(groups = "emulator")
    public void validateApiTypePresent() {
        ApiType apiType = ApiType.TABLE;
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();

        CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode(directConnectionConfig)
            .userAgentSuffix("custom-direct-client")
            .multipleWriteRegionsEnabled(false)
            .endpointDiscoveryEnabled(false)
            .readRequestsFallbackEnabled(true);

         ImplementationBridgeHelpers.CosmosClientBuilderHelper.CosmosClientBuilderAccessor accessor =
            ImplementationBridgeHelpers.CosmosClientBuilderHelper.getCosmosClientBuilderAccessor();
         accessor.setCosmosClientApiType(cosmosClientBuilder, apiType);

        RxDocumentClientImpl documentClient =
            (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(cosmosClientBuilder.buildAsyncClient());
        assertThat(ReflectionUtils.getApiType(documentClient)).isEqualTo(apiType);
    }

    class MyDiagnosticsHandler implements CosmosDiagnosticsHandler {

        @Override
        public void handleDiagnostics(CosmosDiagnosticsContext diagnosticsContext, CosmosDiagnostics diagnostics,
                                      CosmosException error, int statusCode, int subStatusCode) {
            logger.debug(
                "Account: {} -> DB: {}, Col:{}, StatusCode: {}:{} Diagnostics: {}",
                diagnosticsContext.getAccountName(),
                diagnosticsContext.getDatabaseName(),
                diagnosticsContext.getCollectionName(),
                statusCode,
                subStatusCode,
                diagnostics.toString());
        }
    }

    @Test(groups = "unit")
    public void enableLogging() {
        try {
            CosmosAsyncClient client = new CosmosClientBuilder()
                .key(TestConfigurations.MASTER_KEY)
                .endpoint(hostName)
                .preferredRegions(Arrays.asList("westus1,eastus1"))
                .clientTelemetryConfig(
                    new CosmosClientTelemetryConfig()
                        .enableDiagnosticLogs() // default logger to log4j, only logging for point operations > 1 second or feed operation > 3 seconds or > 1000 RU
                        .enableDiagnosticLogs( // change config on which diagnostics to log
                            new CosmosDiagnosticsLoggerConfig()
                                .setPointOperationLatencyThreshold(Duration.ofMillis(500))
                                .setFeedOperationLatencyThreshold(Duration.ofSeconds(10))
                                .setRequestChargeThreshold(100)
                        )
                        .diagnosticsHandler(new MyDiagnosticsHandler()) // using a custom diagnostics handler
                )
                .buildAsyncClient();
            client.close();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e).hasCauseExactlyInstanceOf(URISyntaxException.class);
            assertThat(e.getMessage()).isEqualTo("invalid location [westus1,eastus1] or serviceEndpoint [https://sample-account.documents.azure.com:443/]");
        }
    }
}
