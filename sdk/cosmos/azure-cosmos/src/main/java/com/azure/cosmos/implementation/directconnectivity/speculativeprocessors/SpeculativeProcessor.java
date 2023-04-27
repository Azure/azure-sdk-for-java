// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.models.CosmosEndToEndOperationLatencyPolicyConfig;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public interface SpeculativeProcessor {

    List<URI> getRegionsForPureExploration();
    List<URI> getRegionsToSpeculate(CosmosEndToEndOperationLatencyPolicyConfig config, List<URI> availableReadEndpoints);

    Duration getThreshold(CosmosEndToEndOperationLatencyPolicyConfig config);

    boolean shouldIncludeOriginalRequestRegion();

    void onResponseReceived(URI region, Duration latency);
}
