// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature;

import java.time.Instant;
import java.util.List;

public class FeatureFlagState {

    private final List<FeatureFlags> watchKeys;

    private final Instant nextRefreshCheck;

    private final String originEndpoint;

    private Integer refreshAttempt;

    private final int refreshInterval;

    public FeatureFlagState(List<FeatureFlags> watchKeys, int refreshInterval, String originEndpoint) {
        this.watchKeys = watchKeys;
        this.refreshInterval = refreshInterval;
        nextRefreshCheck = Instant.now().plusSeconds(refreshInterval);
        this.originEndpoint = originEndpoint;
        this.refreshAttempt = 1;
    }

    public FeatureFlagState(FeatureFlagState oldState, Instant newRefresh) {
        this.watchKeys = oldState.getWatchKeys();
        this.refreshInterval = oldState.getRefreshInterval();
        this.nextRefreshCheck = newRefresh;
        this.originEndpoint = oldState.getOriginEndpoint();
        this.refreshAttempt = oldState.getRefreshAttempt();
    }

    /**
     * @return the watchKeys
     */
    public List<FeatureFlags> getWatchKeys() {
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

    /**
     * @return the refreshAttempt
     */
    public Integer getRefreshAttempt() {
        return refreshAttempt;
    }

    /**
     * Adds 1 to the number of refresh attempts
     */
    public void incrementRefreshAttempt() {
        this.refreshAttempt += 1;
    }

    /**
     * @return the refreshInterval
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

}
