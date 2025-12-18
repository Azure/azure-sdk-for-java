// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Instant;
import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.CollectionMonitoring;

class State {

    private final List<ConfigurationSetting> watchKeys;

    private final List<CollectionMonitoring> collectionWatchKeys;

    private final Instant nextRefreshCheck;

    private final String originEndpoint;

    private final int refreshAttempt;

    private final int refreshInterval;

    State(List<ConfigurationSetting> watchKeys, int refreshInterval, String originEndpoint) {
        this(watchKeys, null, refreshInterval, originEndpoint);
    }

    State(List<ConfigurationSetting> watchKeys, List<CollectionMonitoring> collectionWatchKeys, int refreshInterval, String originEndpoint) {
        this(watchKeys, collectionWatchKeys, refreshInterval, originEndpoint, Instant.now().plusSeconds(refreshInterval), 1);
    }

    State(State oldState, Instant newRefresh) {
        this(oldState, newRefresh, oldState.getRefreshAttempt());
    }

    State(State oldState, Instant newRefresh, int refreshAttempt) {
        this(oldState.getWatchKeys(), oldState.getCollectionWatchKeys(), oldState.getRefreshInterval(),
            oldState.getOriginEndpoint(), newRefresh, refreshAttempt);
    }

    // Primary constructor
    private State(List<ConfigurationSetting> watchKeys, List<CollectionMonitoring> collectionWatchKeys,
                  int refreshInterval, String originEndpoint, Instant nextRefreshCheck, int refreshAttempt) {
        this.watchKeys = watchKeys;
        this.collectionWatchKeys = collectionWatchKeys;
        this.refreshInterval = refreshInterval;
        this.nextRefreshCheck = nextRefreshCheck;
        this.originEndpoint = originEndpoint;
        this.refreshAttempt = refreshAttempt;
    }

    /**
     * @return the watchKeys
     */
    public List<ConfigurationSetting> getWatchKeys() {
        return watchKeys;
    }

    /**
     * @return the collectionWatchKeys
     */
    public List<CollectionMonitoring> getCollectionWatchKeys() {
        return collectionWatchKeys;
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
    public int getRefreshAttempt() {
        return refreshAttempt;
    }

    /**
     * Creates a new State with an incremented refresh attempt count
     * @return a new State instance with refreshAttempt incremented by 1
     */
    public State withIncrementedRefreshAttempt() {
        return new State(this, this.nextRefreshCheck, this.refreshAttempt + 1);
    }

    /**
     * @return the refreshInterval
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

}
