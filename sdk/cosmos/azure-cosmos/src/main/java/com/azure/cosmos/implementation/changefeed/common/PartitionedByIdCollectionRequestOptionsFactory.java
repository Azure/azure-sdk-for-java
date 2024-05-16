// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;

import java.time.Duration;

/**
 * Used to create request setOptions for partitioned lease collections, when partition getKey is defined as /getId.
 */
public class PartitionedByIdCollectionRequestOptionsFactory implements RequestOptionsFactory {
    private static CosmosEndToEndOperationLatencyPolicyConfig DISABLED_E2E_TIMEOUT_CONFIG =
        new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(5))
            .enable(false)
            .build();

    @Override
    public CosmosItemRequestOptions createItemRequestOptions(Lease lease) {
        // Disable e2e timeout config within changeFeedProcessor
        return new CosmosItemRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(DISABLED_E2E_TIMEOUT_CONFIG);
    }

    @Override
    public CosmosQueryRequestOptions createQueryRequestOptions() {
        // Disable e2e timeout config within changeFeedProcessor
        return new CosmosQueryRequestOptions().setCosmosEndToEndOperationLatencyPolicyConfig(DISABLED_E2E_TIMEOUT_CONFIG);
    }
}
