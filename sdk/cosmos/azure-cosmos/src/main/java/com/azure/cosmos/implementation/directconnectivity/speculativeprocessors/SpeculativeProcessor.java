// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public interface SpeculativeProcessor {
    int NONE = 0;
    int THRESHOLD_BASED = 1;
    int THOMPSON_SAMPLING_BASED = 2;

    List<URI> getRegionsForPureExploration();
    List<URI> getRegionsToSpeculate(CosmosEndToEndOperationLatencyPolicyConfig config, List<URI> availableReadEndpoints, PartitionKeyRangeIdentity partitionKeyRangeId);

    Duration getThreshold(CosmosEndToEndOperationLatencyPolicyConfig config);

    Duration getThresholdStepDuration(CosmosEndToEndOperationLatencyPolicyConfig config, long stepNumber);

    boolean shouldIncludeOriginalRequestRegion();

    void onResponseReceived(URI region, Duration latency, PartitionKeyRangeIdentity partitionKeyRangeId);
}
