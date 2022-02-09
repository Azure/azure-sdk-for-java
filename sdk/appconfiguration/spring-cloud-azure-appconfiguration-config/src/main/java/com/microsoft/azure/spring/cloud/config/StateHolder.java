// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

final class StateHolder {

    private StateHolder() {
        throw new IllegalStateException("Should not be callable.");
    }

    private static final Map<String, ConfigurationSetting> etagState =
            new ConcurrentHashMap<>();

    private static final Map<String, Boolean> loadState = new ConcurrentHashMap<>();

    /**
     * @return the etagState
     */
    public static ConfigurationSetting getEtagState(String name) {
        return etagState.get(name);
    }

    /**
     * @param etagState the etagState to set
     */
    static void setEtagState(String name, ConfigurationSetting config) {
        etagState.put(name, config);
    }

    /**
     * @return the loadState
     */
    static boolean getLoadState(String name) {
        return loadState.getOrDefault(name, false);
    }

    /**
     * @param loadState the loadState to set
     */
    public static void setLoadState(String name, Boolean loaded) {
        loadState.put(name, loaded);
    }

}
