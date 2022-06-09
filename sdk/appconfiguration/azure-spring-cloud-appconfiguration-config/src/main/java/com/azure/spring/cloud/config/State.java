// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.time.Instant;
import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

class State {

    private final List<ConfigurationSetting> watchKeys;

    private final Instant nextRefreshCheck;

    private final String key;

    private Integer refreshAttempt;

    private final int refreshInterval;

    State(List<ConfigurationSetting> watchKeys, int refreshInterval, String key) {
        this.watchKeys = watchKeys;
        this.refreshInterval = refreshInterval;
        nextRefreshCheck = Instant.now().plusSeconds(refreshInterval);
        this.key = key;
        this.refreshAttempt = 1;
    }

    State(State oldState, Instant newRefresh, String key) {
        this.watchKeys = oldState.getWatchKeys();
        this.refreshInterval = oldState.getRefreshInterval();
        this.nextRefreshCheck = newRefresh;
        this.key = key;
        this.refreshAttempt = oldState.getRefreshAttempt();
    }

    /**
     * @return the watchKeys
     */
    public List<ConfigurationSetting> getWatchKeys() {
        return watchKeys;
    }

    /**
     * @return the nextRefreshCheck
     */
    public Instant getNextRefreshCheck() {
        return nextRefreshCheck;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the refreshAttempt
     */
    public Integer getRefreshAttempt() {
        return refreshAttempt;
    }

    /**
     * @param refreshAttempt the refreshAttempt to set
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
