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

    private Integer refreshAttempt;

    private final int refreshInterval;

    State(List<ConfigurationSetting> watchKeys, int refreshInterval, String originEndpoint) {
        this.watchKeys = watchKeys;
        this.collectionWatchKeys = null;
        this.refreshInterval = refreshInterval;
        nextRefreshCheck = Instant.now().plusSeconds(refreshInterval);
        this.originEndpoint = originEndpoint;
        this.refreshAttempt = 1;
    }

    State(List<ConfigurationSetting> watchKeys, List<CollectionMonitoring> collectionWatchKeys, int refreshInterval, String originEndpoint) {
        this.watchKeys = watchKeys;
        this.collectionWatchKeys = collectionWatchKeys;
        this.refreshInterval = refreshInterval;
        nextRefreshCheck = Instant.now().plusSeconds(refreshInterval);
        this.originEndpoint = originEndpoint;
        this.refreshAttempt = 1;
    }

    State(State oldState, Instant newRefresh) {
        this.watchKeys = oldState.getWatchKeys();
        this.collectionWatchKeys = oldState.getCollectionWatchKeys();
        this.refreshInterval = oldState.getRefreshInterval();
        this.nextRefreshCheck = newRefresh;
        this.originEndpoint = oldState.getOriginEndpoint();
        this.refreshAttempt = oldState.getRefreshAttempt();
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
