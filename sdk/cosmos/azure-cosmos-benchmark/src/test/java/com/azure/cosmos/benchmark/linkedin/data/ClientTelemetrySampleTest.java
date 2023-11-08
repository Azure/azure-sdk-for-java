// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark.linkedin.data;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;

/**
 * This is a sample test case, not to be executed
 */
public class ClientTelemetrySampleTest {

    /**
     * Enable Client Telemetry: 2 steps are required
     *
     * 1. Enable the flag through `CosmosClientBuilder`.
     *
     * 2. Set the system property either through code - `System.setProperty("COSMOS.CLIENT_TELEMETRY_ENDPOINT", $endpoint)` or through JVM arguments - `-DCOSMOS.CLIENT_TELEMETRY_ENDPOINT=$endpoint`
     *
     * Usage of Client Telemetry Endpoint in various environments:
     *   Production Endpoint: https://tools.cosmos.azure.com/api/clienttelemetry/trace _(recommended)_
     *   Stage Endpoint: https://tools-staging.cosmos.azure.com/api/clienttelemetry/trace/ _(recommended for local Testing)_
     *   Test Endpoint: https://juno-test.documents-dev.windows-int.net/api/clienttelemetry/trace/ _(Allows only dogfood accounts i.e. https://df.onecloud.azure-test.net/)_
     */
    private void enableClientTelemetry() {
        String clientTelemetryProductionEndpoint = "https://tools.cosmos.azure.com/api/clienttelemetry/trace/";
        String clientTelemetryStagingEndpoint = "https://tools-staging.cosmos.azure.com/api/clienttelemetry/trace/";
        String clientTelemetryTestEndpoint = "https://juno-test.documents-dev.windows-int.net/api/clienttelemetry/trace/";
        System.setProperty("COSMOS.CLIENT_TELEMETRY_ENDPOINT", clientTelemetryStagingEndpoint);
        System.setProperty("COSMOS.CLIENT_TELEMETRY_PROXY_OPTIONS_CONFIG", "{\"type\":\"HTTP\", \"host\": \"localhost\", \"port\": 8080}");

        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .directMode()
            .clientTelemetryEnabled(true)
            .buildClient();
    }

    /**
     * Disallow Query Plan Generation for single partition queries
     */
    private void disallowQueryPlanGeneration() {
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        cosmosQueryRequestOptions.setPartitionKey(new PartitionKey("samplePk"));

        ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.CosmosQueryRequestOptionsAccessor cosmosQueryRequestOptionsAccessor = ImplementationBridgeHelpers
            .CosmosQueryRequestOptionsHelper
            .getCosmosQueryRequestOptionsAccessor();
        cosmosQueryRequestOptionsAccessor.disallowQueryPlanRetrieval(cosmosQueryRequestOptions);
    }
}
