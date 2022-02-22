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

    State(List<ConfigurationSetting> watchKeys, int refreshInterval, String key) {
        this.watchKeys = watchKeys;
        nextRefreshCheck = Instant.now().plusSeconds(refreshInterval);
        this.key = key;
    }
    
    State(State oldState, Instant newRefresh, String key) {
        this.watchKeys = oldState.getWatchKeys();
        this.nextRefreshCheck = newRefresh;
        this.key = key;
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
}
