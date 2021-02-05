/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

final class StateHolder {

    private StateHolder() {
        throw new IllegalStateException("Should not be callable.");
    }

    private static ConcurrentHashMap<String, ConfigurationSetting> etagState = 
            new ConcurrentHashMap<String, ConfigurationSetting>();

    private static ConcurrentHashMap<String, Boolean> loadState = new ConcurrentHashMap<String, Boolean>();

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
    public static Boolean getLoadState(String name) {
        Boolean loadstate = loadState.get(name);
        return loadstate == null ? false : loadstate;
    }

    /**
     * @param loadState the loadState to set
     */
    public static void setLoadState(String name, Boolean loaded) {
        loadState.put(name, loaded);
    }

}
