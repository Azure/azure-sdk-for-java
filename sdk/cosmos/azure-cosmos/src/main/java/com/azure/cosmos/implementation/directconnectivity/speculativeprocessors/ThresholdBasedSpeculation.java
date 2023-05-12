// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.speculativeprocessors;

import com.azure.cosmos.CosmosEndToEndOperationRetryPolicyConfig;
import com.azure.cosmos.implementation.Configs;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public class ThresholdBasedSpeculation implements SpeculativeProcessor{
    @Override
    public List<URI> getRegionsForPureExploration() {
        return null;
    }

    @Override
    public List<URI> getRegionsToSpeculate(CosmosEndToEndOperationRetryPolicyConfig config, List<URI> availableReadEndpoints) {
        return availableReadEndpoints;
    }

    @Override
    public Duration getThreshold(CosmosEndToEndOperationRetryPolicyConfig config) {
        return Duration.ofMillis(Configs.speculationThreshold());
    }

    @Override
    public Duration getThresholdStepDuration(CosmosEndToEndOperationRetryPolicyConfig config, long stepNumber) {
        return Duration.ofMillis(stepNumber * Configs.speculationThresholdStep());
    }

    @Override
    public boolean shouldIncludeOriginalRequestRegion() {
        return true;
    }

    @Override
    public void onResponseReceived(URI region, Duration latency) {
        // unused
    }
}
