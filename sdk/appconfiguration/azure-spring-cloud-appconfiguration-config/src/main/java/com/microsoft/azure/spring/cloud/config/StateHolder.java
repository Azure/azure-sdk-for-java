// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

final class StateHolder {

    private static final int MAX_JITTER = 15;
    private static final String FEATURE_ENDPOINT = "_feature";
    private static final Map<String, State> state = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> loadState = new ConcurrentHashMap<>();

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
     * @return the state
     */
    static State getStateFeatureFlag(String endpoint) {
        return state.get(endpoint + FEATURE_ENDPOINT);
    }

    /**
     * @param endpoint   the stores endpoint
     * @param watchKeys  list of configuration watch keys that can trigger a refresh event
     * @param monitoring refresh configurations
     */
    static void setState(String endpoint, List<ConfigurationSetting> watchKeys,
        Duration duration) {
        state.put(endpoint, new State(watchKeys, Math.toIntExact(duration.getSeconds())));
    }
    
    /**
     * @param endpoint   the stores endpoint
     * @param watchKeys  list of configuration watch keys that can trigger a refresh event
     * @param monitoring refresh configurations
     */
    static void setStateFeatureFlag(String endpoint, List<ConfigurationSetting> watchKeys,
        Duration duration) {
        setState(endpoint + FEATURE_ENDPOINT, watchKeys, duration);
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
        return loadState.getOrDefault(name,  false);
    }
    
    /**
     * @return the loadState
     */
    static boolean getLoadStateFeatureFlag(String name) {
        return getLoadState(name + FEATURE_ENDPOINT);
    }

    /**
     * @param loadState the loadState to set
     */
    static void setLoadState(String name, Boolean loaded) {
        loadState.put(name, loaded);
    }
    
    /**
     * @param loadState the loadState to set
     */
    static void setLoadStateFeatureFlag(String name, Boolean loaded) {
        setLoadState(name + FEATURE_ENDPOINT, loaded);
    }
}
