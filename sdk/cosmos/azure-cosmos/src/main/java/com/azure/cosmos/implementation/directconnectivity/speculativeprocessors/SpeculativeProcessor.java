// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.CosmosEndToEndOperationRetryPolicyConfig;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public interface SpeculativeProcessor {
    int NONE = 0;
    int THRESHOLD_BASED = 1;
    int THOMPSON_SAMPLING_BASED = 2;

    List<URI> getRegionsForPureExploration();
    List<URI> getRegionsToSpeculate(CosmosEndToEndOperationRetryPolicyConfig config, List<URI> availableReadEndpoints);

    Duration getThreshold(CosmosEndToEndOperationRetryPolicyConfig config);

    Duration getThresholdStepDuration(CosmosEndToEndOperationRetryPolicyConfig config, long stepNumber);

    boolean shouldIncludeOriginalRequestRegion();

    void onResponseReceived(URI region, Duration latency);
}
