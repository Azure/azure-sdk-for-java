// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature;

import java.time.Instant;
import java.util.List;

import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.CollectionMonitoring;

public class FeatureFlagState {

    private final List<CollectionMonitoring> watchKeys;

    private final Instant nextRefreshCheck;

    private final String originEndpoint;

    public FeatureFlagState(List<CollectionMonitoring> watchKeys, int refreshInterval, String originEndpoint) {
        this.watchKeys = watchKeys;
        nextRefreshCheck = Instant.now().plusSeconds(refreshInterval);
        this.originEndpoint = originEndpoint;
    }

    public FeatureFlagState(FeatureFlagState oldState, Instant newRefresh) {
        this.watchKeys = oldState.getWatchKeys();
        this.nextRefreshCheck = newRefresh;
        this.originEndpoint = oldState.getOriginEndpoint();
    }

    /**
     * @return the watchKeys
     */
    public List<CollectionMonitoring> getWatchKeys() {
        return watchKeys;
    }

    /**
     * @return the nextRefreshCheck
     */
    public Instant getNextRefreshCheck() {
        return nextRefreshCheck;
    }

    /**
     * @return the originEndpoint
     */
    public String getOriginEndpoint() {
        return originEndpoint;
    }

}
