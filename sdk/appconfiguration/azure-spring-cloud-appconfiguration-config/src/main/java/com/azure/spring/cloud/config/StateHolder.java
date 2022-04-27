// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;

final class StateHolder {

    private static final int MAX_JITTER = 15;

    private static final String FEATURE_ENDPOINT = "_feature";

    private static final Map<String, State> STATE = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> LOAD_STATE = new ConcurrentHashMap<>();

    private static Integer clientRefreshAttempts = 1;

    private static Instant nextForcedRefresh;

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
     * @param duration refresh duration.
     */
    static void setState(String endpoint, List<ConfigurationSetting> watchKeys,
        Duration duration) {
        STATE.put(endpoint, new State(watchKeys, Math.toIntExact(duration.getSeconds()), endpoint));
    }

    /**
     * @param endpoint the stores endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param duration refresh duration.
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
        STATE.put(state.getKey(),
            new State(state.getWatchKeys(), Math.toIntExact(duration.getSeconds()), state.getKey()));
    }

    static void expireState(String endpoint) {
        State oldState = STATE.get(endpoint);
        SecureRandom random = new SecureRandom();
        long wait = (long) (random.nextDouble() * MAX_JITTER);
        long timeLeft = (int) ((oldState.getNextRefreshCheck().toEpochMilli() - (Instant.now().toEpochMilli())) / 1000);
        if (wait < timeLeft) {
            STATE.put(endpoint, new State(oldState.getWatchKeys(), (int) wait, oldState.getKey()));
        }
    }

    /**
     * @return the loadState
     */
    static boolean getLoadState(String name) {
        return LOAD_STATE.getOrDefault(name, false);
    }

    /**
     * @return the loadState
     */
    static boolean getLoadStateFeatureFlag(String name) {
        return getLoadState(name + FEATURE_ENDPOINT);
    }

    /**
     * @param name the loadState name to set
     */
    static void setLoadState(String name, Boolean loaded) {
        LOAD_STATE.put(name, loaded);
    }

    /**
     * @param name the loadState feature flag name to set
     */
    static void setLoadStateFeatureFlag(String name, Boolean loaded) {
        setLoadState(name + FEATURE_ENDPOINT, loaded);
    }

    /**
     * @return the nextForcedRefresh
     */
    public static Instant getNextForcedRefresh() {
        return nextForcedRefresh;
    }

    /**
     * Set after load or refresh is successful.
     * @param refreshPeriod the refreshPeriod to set
     */
    public static void setNextForcedRefresh(Duration refreshPeriod) {
        nextForcedRefresh = Instant.now().plusSeconds(refreshPeriod.getSeconds());
    }

    /**
     * Sets a minimum value until the next refresh. If a refresh interval has passed or is smaller than the calculated
     * backoff time, the refresh interval is set to the backoff time.
     * @param refreshInterval period between refresh checks.
     * @param properties Provider properties for min and max backoff periods.
     */
    static void updateNextRefreshTime(Duration refreshInterval, AppConfigurationProviderProperties properties) {
        if (refreshInterval != null) {
            Instant newForcedRefresh = getNextRefreshCheck(nextForcedRefresh,
                clientRefreshAttempts, refreshInterval.getSeconds(), properties);

            if (newForcedRefresh.compareTo(nextForcedRefresh) != 0) {
                clientRefreshAttempts += 1;
            }
            nextForcedRefresh = newForcedRefresh;
        }

        for (Entry<String, State> entry : STATE.entrySet()) {
            State state = entry.getValue();
            Instant newRefresh = getNextRefreshCheck(state.getNextRefreshCheck(),
                state.getRefreshAttempt(), (long) state.getRefreshInterval(), properties);

            if (newRefresh.compareTo(entry.getValue().getNextRefreshCheck()) != 0) {
                state.incrementRefreshAttempt();
            }
            State updatedState = new State(state, newRefresh, entry.getKey());
            STATE.put(entry.getKey(), updatedState);
        }
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys. Used for checking client refresh-interval only.
     * @param nextRefreshCheck next refresh for the whole client
     * @param attempt refresh attempt for the client
     * @param interval the Refresh Interval
     * @param properties App Configuration Provider Properties
     * @return new Refresh Date
     */
    private static Instant getNextRefreshCheck(Instant nextRefreshCheck, Integer attempt, Long interval,
        AppConfigurationProviderProperties properties) {
        // The refresh interval is only updated if it is expired.
        if (!Instant.now().isAfter(nextRefreshCheck)) {
            return nextRefreshCheck;
        }

        int durationPeriod = Math.toIntExact(interval);

        Instant now = Instant.now();

        if (durationPeriod <= properties.getDefaultMinBackoff()) {
            return now.plusSeconds(interval);
        }

        return now.plusNanos(
            BackoffTimeCalculator.calculateBackoff(attempt, interval, properties.getDefaultMaxBackoff(), properties.getDefaultMinBackoff()));
    }

    static void clearAttempts() {
        clientRefreshAttempts = 1;
    }

}
