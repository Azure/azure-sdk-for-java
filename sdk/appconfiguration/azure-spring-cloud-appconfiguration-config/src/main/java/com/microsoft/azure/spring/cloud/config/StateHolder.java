// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

final class StateHolder {

    private static final int MAX_JITTER = 15;
    private static ConcurrentHashMap<String, State> state = new ConcurrentHashMap<String, State>();
    private static ConcurrentHashMap<String, Boolean> loadState = new ConcurrentHashMap<String, Boolean>();

    private StateHolder() {
        throw new IllegalStateException("Should not be callable.");
    }

    /**
     * @return the state
     */
    static State getState(String endpoint) {
        return state.get(endpoint);
    }

    /**
     * @param endpoint   the stores endpoint
     * @param watchKeys  list of configuration watch keys that can trigger a refresh event
     * @param monitoring refresh configurations
     */
    static void setState(String endpoint, List<ConfigurationSetting> watchKeys,
        AppConfigurationStoreMonitoring monitoring) {
        state.put(endpoint, new State(watchKeys, Math.toIntExact(monitoring.getCacheExpiration().getSeconds())));
    }

    static void expireState(String endpoint) {
        String key = endpoint;
        State oldState = state.get(key);
        SecureRandom random = new SecureRandom();
        long wait = (long) (random.nextDouble() * MAX_JITTER);
        long timeLeft = (int) ((oldState.getNotCachedTime().getTime() - (new Date().getTime())) / 1000);
        if (wait < timeLeft) {
            state.put(key, new State(oldState.getWatchKeys(), (int) wait));
        }
    }

    /**
     * @return the loadState
     */
    static boolean getLoadState(String name) {
        Boolean loadstate = loadState.get(name);
        return loadstate == null ? false : loadstate;
    }

    /**
     * @param loadState the loadState to set
     */
    static void setLoadState(String name, Boolean loaded) {
        loadState.put(name, loaded);
    }
}
