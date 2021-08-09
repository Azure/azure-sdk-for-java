// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

class State {

    private final List<ConfigurationSetting> watchKeys;

    private final Date nextRefreshCheck;
    
    private final String key;

    State(List<ConfigurationSetting> watchKeys, int refreshInterval, String key) {
        this.watchKeys = watchKeys;
        nextRefreshCheck = DateUtils.addSeconds(new Date(), refreshInterval);
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
    public Date getNextRefreshCheck() {
        return nextRefreshCheck;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
}
