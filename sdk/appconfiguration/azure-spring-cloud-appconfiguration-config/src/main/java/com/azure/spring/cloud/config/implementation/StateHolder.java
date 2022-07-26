// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

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

    private static StateHolder currentState;

    private final Map<String, State> state = new ConcurrentHashMap<>();

    private final Map<String, Boolean> loadState = new ConcurrentHashMap<>();

    private Integer clientRefreshAttempts = 1;

    private Instant nextForcedRefresh;

    StateHolder() {
    }

    static StateHolder getCurrentState() {
        return currentState;
    }

    static void updateState(StateHolder newState) {
        currentState = newState;
    }

    /**
     * @param originEndpoint the endpoint for the origin config store
     * @return the state
     */
    static State getState(String originEndpoint) {
        return currentState.getFullState().get(originEndpoint);
    }

    private Map<String, State> getFullState() {
        return state;
    }

    private Map<String, Boolean> getFullLoadState() {
        return loadState;
    }

    /**
     * @param originEndpoint the endpoint for the origin config store
     * @return the state
     */
    static State getStateFeatureFlag(String originEndpoint) {
        return currentState.getFullState().get(originEndpoint + FEATURE_ENDPOINT);
    }

    /**
     * @param originEndpoint the stores origin endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param duration refresh duration.
     */
    void setState(String originEndpoint, List<ConfigurationSetting> watchKeys, Duration duration) {
        state.put(originEndpoint, new State(watchKeys, Math.toIntExact(duration.getSeconds()), originEndpoint));
    }

    /**
     * @param originEndpoint the stores origin endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param duration refresh duration.
     */
    void setStateFeatureFlag(String originEndpoint, List<ConfigurationSetting> watchKeys,
        Duration duration) {
        setState(originEndpoint + FEATURE_ENDPOINT, watchKeys, duration);
    }

    /**
     * @param state previous state to base off
     * @param duration nextRefreshPeriod
     */
    void setState(State state, Duration duration) {
        this.state.put(state.getOriginEndpoint(),
            new State(state, Instant.now().plusSeconds(Math.toIntExact(duration.getSeconds()))));
    }

    void updateStateRefresh(State state, Duration duration) {
        this.state.put(state.getOriginEndpoint(),
            new State(state, Instant.now().plusSeconds(Math.toIntExact(duration.getSeconds()))));
    }

    void expireState(String originEndpoint) {
        State oldState = state.get(originEndpoint);
        long wait = (long) (new SecureRandom().nextDouble() * MAX_JITTER);

        long timeLeft = (int) ((oldState.getNextRefreshCheck().toEpochMilli() - (Instant.now().toEpochMilli())) / 1000);
        if (wait < timeLeft) {
            state.put(originEndpoint, new State(oldState, Instant.now().plusSeconds(wait)));
        }
    }

    /**
     * @return the loadState
     */
    static boolean getLoadState(String originEndpoint) {
        return currentState.getFullLoadState().getOrDefault(originEndpoint, false);
    }

    /**
     * @return the loadState
     */
    static boolean getLoadStateFeatureFlag(String originEndpoint) {
        return getLoadState(originEndpoint + FEATURE_ENDPOINT);
    }

    Map<String, Boolean> getLoadState() {
        return loadState;
    }

    /**
     * @param name the loadState name to set
     */
    void setLoadState(String originEndpoint, Boolean loaded) {
        loadState.put(originEndpoint, loaded);
    }

    /**
     * @param name the loadState feature flag name to set
     */
    void setLoadStateFeatureFlag(String originEndpoint, Boolean loaded) {
        setLoadState(originEndpoint + FEATURE_ENDPOINT, loaded);
    }

    /**
     * @return the nextForcedRefresh
     */
    public static Instant getNextForcedRefresh() {
        return currentState.nextForcedRefresh;
    }

    /**
     * Set after load or refresh is successful.
     * @param refreshPeriod the refreshPeriod to set
     */
    public void setNextForcedRefresh(Duration refreshPeriod) {
        if (refreshPeriod != null) {
            nextForcedRefresh = Instant.now().plusSeconds(refreshPeriod.getSeconds());
        }
    }

    /**
     * Sets a minimum value until the next refresh. If a refresh interval has passed or is smaller than the calculated
     * backoff time, the refresh interval is set to the backoff time.
     * @param refreshInterval period between refresh checks.
     * @param properties Provider properties for min and max backoff periods.
     */
    void updateNextRefreshTime(Duration refreshInterval, AppConfigurationProviderProperties properties) {
        if (refreshInterval != null) {
            Instant newForcedRefresh = getNextRefreshCheck(nextForcedRefresh,
                clientRefreshAttempts, refreshInterval.getSeconds(), properties);

            if (newForcedRefresh.compareTo(nextForcedRefresh) != 0) {
                clientRefreshAttempts += 1;
            }
            nextForcedRefresh = newForcedRefresh;
        }

        for (Entry<String, State> entry : state.entrySet()) {
            State state = entry.getValue();
            Instant newRefresh = getNextRefreshCheck(state.getNextRefreshCheck(),
                state.getRefreshAttempt(), (long) state.getRefreshInterval(), properties);

            if (newRefresh.compareTo(entry.getValue().getNextRefreshCheck()) != 0) {
                state.incrementRefreshAttempt();
            }
            State updatedState = new State(state, newRefresh);
            this.state.put(entry.getKey(), updatedState);
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
    private Instant getNextRefreshCheck(Instant nextRefreshCheck, Integer attempt, Long interval,
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
            BackoffTimeCalculator.calculateBackoff(attempt, properties.getDefaultMaxBackoff(),
                properties.getDefaultMinBackoff()));
    }

}
