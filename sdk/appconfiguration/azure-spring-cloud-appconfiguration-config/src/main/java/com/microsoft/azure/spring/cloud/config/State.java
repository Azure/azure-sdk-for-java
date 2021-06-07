// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;

class State {

    private final List<ConfigurationSetting> watchKeys;

    private final Date notCachedTime;

    State(List<ConfigurationSetting> watchKeys, int cacheExpirationTime) {
        this.watchKeys = watchKeys;
        notCachedTime = DateUtils.addSeconds(new Date(), cacheExpirationTime);
    }

    /**
     * @return the watchKeys
     */
    public List<ConfigurationSetting> getWatchKeys() {
        return watchKeys;
    }

    /**
     * @return the notCachedTime
     */
    public Date getNotCachedTime() {
        return notCachedTime;
    }

}
