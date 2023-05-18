// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public class ThresholdBasedSpeculation implements SpeculativeProcessor{
    @Override
    public List<URI> getRegionsForPureExploration() {
        return null;
    }

    @Override
    public List<URI> getRegionsToSpeculate(CosmosEndToEndOperationLatencyPolicyConfig config, List<URI> availableReadEndpoints, PartitionKeyRangeIdentity partitionKeyRangeId) {
        return availableReadEndpoints;
    }

    @Override
    public Duration getThreshold(CosmosEndToEndOperationLatencyPolicyConfig config) {
        return Duration.ofMillis(Configs.speculationThreshold());
    }

    @Override
    public Duration getThresholdStepDuration(CosmosEndToEndOperationLatencyPolicyConfig config, long stepNumber) {
        return Duration.ofMillis(stepNumber * Configs.speculationThresholdStep());
    }

    @Override
    public boolean shouldIncludeOriginalRequestRegion() {
        return true;
    }

    @Override
    public void onResponseReceived(URI region, Duration latency, PartitionKeyRangeIdentity partitionKeyRangeId) {
        throw new NotImplementedException("onResponseReceived is not required for ThresholdBasedSpeculation");
    }
}
