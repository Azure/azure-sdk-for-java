// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.ISessionContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RegionScopedSessionContainer;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosClientBuilderTest {
    String hostName = "https://sample-account.documents.azure.com:443/";

    @DataProvider(name = "regionScopedSessionContainerConfigs")
    public Object[] regionScopedSessionContainerConfigs() {
        return new Object[] {false, true};
    }

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

        // trying to enable via telemetryConfig - should fail because the same CosmosClientTelemetryConfig instance
        // has been used to built another Cosmos Client - enabling/disabling ClientTelemetry is immutable right now
        telemetryConfig.sendClientTelemetryToService(true);
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();

        assertThat(telemetryAccessor.isSendClientTelemetryToServiceEnabled(client.getClientTelemetryConfig()))
            .isEqualTo(false);
        client.close();

        // Enabling via telemetryConfig and disabled via builder.isClientTelemetryEnabled --> Disabled
        telemetryConfig = new CosmosClientTelemetryConfig();
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
        telemetryConfig = new CosmosClientTelemetryConfig();
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
        telemetryConfig = new CosmosClientTelemetryConfig();
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
        telemetryConfig = new CosmosClientTelemetryConfig();
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
        telemetryConfig = new CosmosClientTelemetryConfig();
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

    @Test(groups = "emulator", dataProvider = "regionScopedSessionContainerConfigs")
    public void validateSessionTokenCapturingForAccountDefaultConsistency(boolean shouldRegionScopedSessionContainerEnabled) {

        try {

            if (shouldRegionScopedSessionContainerEnabled) {
                System.setProperty("COSMOS.SESSION_CAPTURING_TYPE", "REGION_SCOPED");
            }

            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .userAgentSuffix("custom-direct-client");

            CosmosAsyncClient client = cosmosClientBuilder.buildAsyncClient();
            RxDocumentClientImpl documentClient =
                (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);

            if (documentClient.getDefaultConsistencyLevelOfAccount() != ConsistencyLevel.SESSION) {
                throw new SkipException("This test is only applicable when default account-level consistency is Session.");
            }

            ISessionContainer sessionContainer = documentClient.getSession();

            if (System.getProperty("COSMOS.SESSION_CAPTURING_TYPE") != null && System.getProperty("COSMOS.SESSION_CAPTURING_TYPE").equals("REGION_SCOPED")) {
                assertThat(sessionContainer instanceof RegionScopedSessionContainer).isTrue();
            } else {
                assertThat(sessionContainer instanceof SessionContainer).isTrue();
            }

            assertThat(sessionContainer.getDisableSessionCapturing()).isEqualTo(false);
        } finally {
            System.clearProperty("COSMOS.SESSION_CAPTURING_TYPE");
        }
    }

    // set env variable to COSMOS.SESSION_CAPTURING_TYPE to REGION_SCOPED to test all possible assertions
    @Test(groups = "unit", enabled = false)
    public void validateSessionTokenCapturingForAccountDefaultConsistencyWithEnvVariable() {

        try {

            CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .userAgentSuffix("custom-direct-client");

            CosmosAsyncClient client = cosmosClientBuilder.buildAsyncClient();
            RxDocumentClientImpl documentClient =
                (RxDocumentClientImpl) ReflectionUtils.getAsyncDocumentClient(client);

            if (documentClient.getDefaultConsistencyLevelOfAccount() != ConsistencyLevel.SESSION) {
                throw new SkipException("This test is only applicable when default account-level consistency is Session.");
            }

            ISessionContainer sessionContainer = documentClient.getSession();

            if (System.getenv("COSMOS.SESSION_CAPTURING_TYPE") != null && System.getenv("COSMOS.SESSION_CAPTURING_TYPE").equals("REGION_SCOPED")) {
                assertThat(sessionContainer instanceof RegionScopedSessionContainer).isTrue();
            } else {
                assertThat(sessionContainer instanceof SessionContainer).isTrue();
            }

            assertThat(sessionContainer.getDisableSessionCapturing()).isEqualTo(false);
        } finally {
            System.clearProperty("COSMOS.SESSION_CAPTURING_TYPE");
        }
    }
}
