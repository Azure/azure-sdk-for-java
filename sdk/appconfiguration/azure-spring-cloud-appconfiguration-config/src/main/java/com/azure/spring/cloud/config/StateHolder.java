// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

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
    private static final Map<String, State> STATE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> LOAD_STATE = new ConcurrentHashMap<>();

    private StateHolder() {
        throw new IllegalStateException("Should not be callable.");
    }

    /**
     * @return the state
     */
    static State getState(String endpoint) {
        return STATE.get(endpoint);
    }
    
    /**
     * @return the state
     */
    static State getStateFeatureFlag(String endpoint) {
        return STATE.get(endpoint + FEATURE_ENDPOINT);
    }

    /**
     * @param endpoint the stores endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param monitoring refresh configurations
     */
    static void setState(String endpoint, List<ConfigurationSetting> watchKeys,
        Duration duration) {
        STATE.put(endpoint, new State(watchKeys, Math.toIntExact(duration.getSeconds()), endpoint));
    }
    
    /**
     * @param endpoint the stores endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param monitoring refresh configurations
     */
    static void setStateFeatureFlag(String endpoint, List<ConfigurationSetting> watchKeys,
        Duration duration) {
        setState(endpoint + FEATURE_ENDPOINT, watchKeys, duration);
    }

    /**
     * @param state previous state to base off
     * @param duration nextRefreshPeriod
     */
    static void setState(State state, Duration duration) {
        STATE.put(state.getKey(), new State(state.getWatchKeys(), Math.toIntExact(duration.getSeconds()), state.getKey()));
    }

    static void expireState(String endpoint) {
        String key = endpoint;
        State oldState = STATE.get(key);
        SecureRandom random = new SecureRandom();
        long wait = (long) (random.nextDouble() * MAX_JITTER);
        long timeLeft = (int) ((oldState.getNextRefreshCheck().getTime() - (new Date().getTime())) / 1000);
        if (wait < timeLeft) {
            STATE.put(key, new State(oldState.getWatchKeys(), (int) wait, oldState.getKey()));
        }
    }

    /**
     * @return the loadState
     */
    static boolean getLoadState(String name) {
        return LOAD_STATE.getOrDefault(name,  false);
    }
    
    /**
     * @return the loadState
     */
    static boolean getLoadStateFeatureFlag(String name) {
        return getLoadState(name + FEATURE_ENDPOINT);
    }

    /**
     * @param LOAD_STATE the loadState to set
     */
    static void setLoadState(String name, Boolean loaded) {
        LOAD_STATE.put(name, loaded);
    }
    
    /**
     * @param LOAD_STATE the loadState to set
     */
    static void setLoadStateFeatureFlag(String name, Boolean loaded) {
        setLoadState(name + FEATURE_ENDPOINT, loaded);
    }
}
