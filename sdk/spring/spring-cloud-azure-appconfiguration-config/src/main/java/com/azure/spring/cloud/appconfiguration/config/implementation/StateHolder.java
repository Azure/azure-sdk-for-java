// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.CollectionMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlagState;

/**
 * Thread-safe singleton holder for managing refresh state of Azure App Configuration stores.
 * 
 * <p>Maintains state for configuration settings, feature flags, and refresh intervals across
 * multiple configuration stores. Implements exponential backoff for failed refresh attempts
 * and coordinates the timing of refresh operations.</p>
 * 
 * <p>Thread Safety: Uses ConcurrentHashMap for all state maps to ensure thread-safe access
 * in multi-threaded environments.</p>
 */
final class StateHolder {

    /** Maximum jitter in seconds to add when expiring state to prevent thundering herd. */
    private static final int MAX_JITTER = 15;

    /** The current singleton instance of StateHolder. */
    private static StateHolder currentState;

    /** Map of configuration store endpoints to their refresh state. */
    private final Map<String, State> state = new ConcurrentHashMap<>();

    /** Map of configuration store endpoints to their feature flag refresh state. */
    private final Map<String, FeatureFlagState> featureFlagState = new ConcurrentHashMap<>();

    /** Map tracking whether each configuration store has been successfully loaded. */
    private final Map<String, Boolean> loadState = new ConcurrentHashMap<>();

    /** Number of client-level refresh attempts for backoff calculation. */
    private Integer clientRefreshAttempts = 1;

    /** The next time a forced refresh should occur across all stores. */
    private Instant nextForcedRefresh;

    StateHolder() {
    }

    /**
     * Gets the current singleton instance of StateHolder.
     * @return the current StateHolder instance, or null if not yet initialized
     */
    static StateHolder getCurrentState() {
        return currentState;
    }

    /**
     * Updates the singleton instance to a new StateHolder.
     * @param newState the new StateHolder instance to set as current
     * @return the updated StateHolder instance
     */
    static StateHolder updateState(StateHolder newState) {
        currentState = newState;
        return currentState;
    }

    /**
     * Retrieves the refresh state for a specific configuration store.
     * @param originEndpoint the endpoint for the origin config store
     * @return the State for the specified store, or null if not found
     */
    static State getState(String originEndpoint) {
        return currentState.getFullState().get(originEndpoint);
    }

    /**
     * Gets the full map of configuration store states.
     * @return map of endpoint to State
     */
    private Map<String, State> getFullState() {
        return state;
    }

    /**
     * Gets the full map of feature flag states.
     * @return map of endpoint to FeatureFlagState
     */
    private Map<String, FeatureFlagState> getFullFeatureFlagState() {
        return featureFlagState;
    }

    /**
     * Gets the full map of load states.
     * @return map of endpoint to load status
     */
    private Map<String, Boolean> getFullLoadState() {
        return loadState;
    }

    /**
     * Retrieves the feature flag refresh state for a specific configuration store.
     * @param originEndpoint the endpoint for the origin config store
     * @return the FeatureFlagState for the specified store, or null if not found
     */
    static FeatureFlagState getStateFeatureFlag(String originEndpoint) {
        return currentState.getFullFeatureFlagState().get(originEndpoint);
    }

    /**
     * Sets the refresh state for a configuration store.
     * @param originEndpoint the store's origin endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param duration refresh duration
     */
    void setState(String originEndpoint, List<ConfigurationSetting> watchKeys, Duration duration) {
        state.put(originEndpoint, new State(watchKeys, Math.toIntExact(duration.getSeconds()), originEndpoint));
    }

    /**
     * Sets the refresh state for a configuration store with collection monitoring.
     * @param originEndpoint the store's origin endpoint
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param collectionWatchKeys list of collection monitoring configurations that can trigger a refresh event
     * @param duration refresh duration
     */
    void setState(String originEndpoint, List<ConfigurationSetting> watchKeys, List<CollectionMonitoring> collectionWatchKeys, Duration duration) {
        state.put(originEndpoint, new State(watchKeys, collectionWatchKeys, Math.toIntExact(duration.getSeconds()), originEndpoint));
    }

    /**
     * Sets the feature flag refresh state for a configuration store.
     * @param originEndpoint the store's origin endpoint
     * @param watchKeys list of feature flag watch keys that can trigger a refresh event
     * @param duration refresh duration
     */
    void setStateFeatureFlag(String originEndpoint, List<CollectionMonitoring> watchKeys,
        Duration duration) {
        featureFlagState.put(originEndpoint,
            new FeatureFlagState(watchKeys, Math.toIntExact(duration.getSeconds()), originEndpoint));
    }

    /**
     * Updates the configuration state with a new refresh time based on the duration.
     * @param state the current State to update
     * @param duration the duration to add to the current time for the next refresh
     */
    void updateStateRefresh(State state, Duration duration) {
        this.state.put(state.getOriginEndpoint(),
            new State(state, Instant.now().plusSeconds(Math.toIntExact(duration.getSeconds()))));
    }

    /**
     * Updates the feature flag state with a new refresh time based on the duration.
     * @param state the current FeatureFlagState to update
     * @param duration the duration to add to the current time for the next refresh
     */
    void updateFeatureFlagStateRefresh(FeatureFlagState state, Duration duration) {
        this.featureFlagState.put(state.getOriginEndpoint(),
            new FeatureFlagState(state, Instant.now().plusSeconds(Math.toIntExact(duration.getSeconds()))));
    }

    /**
     * Expires the state for a configuration store by setting a new refresh time with random jitter.
     * The jitter helps prevent thundering herd when multiple stores refresh simultaneously.
     * @param originEndpoint the endpoint of the store to expire
     */
    void expireState(String originEndpoint) {
        State oldState = state.get(originEndpoint);
        long wait = (long) (new SecureRandom().nextDouble() * MAX_JITTER);

        long timeLeft = (int) ((oldState.getNextRefreshCheck().toEpochMilli() - (Instant.now().toEpochMilli())) / 1000);
        if (wait < timeLeft) {
            state.put(originEndpoint, new State(oldState, Instant.now().plusSeconds(wait)));
        }
    }

    /**
     * Checks if a configuration store has been successfully loaded.
     * @param originEndpoint the endpoint of the store to check
     * @return true if the store has been loaded, false otherwise
     */
    static boolean getLoadState(String originEndpoint) {
        return currentState.getFullLoadState().getOrDefault(originEndpoint, false);
    }

    /**
     * @param originEndpoint the configuration store connected to.
     * @param loaded true if the configuration store was loaded.
     */
    void setLoadState(String originEndpoint, Boolean loaded) {
        loadState.put(originEndpoint, loaded);
    }

    /**
     * Gets the next time a forced refresh should occur across all stores.
     * @return the Instant of the next forced refresh, or null if not set
     */
    public static Instant getNextForcedRefresh() {
        return currentState.nextForcedRefresh;
    }

    /**
     * Sets the next forced refresh time. Called after a successful load or refresh.
     * @param refreshPeriod the duration from now until the next forced refresh; if null, no refresh is scheduled
     */
    public void setNextForcedRefresh(Duration refreshPeriod) {
        if (refreshPeriod != null) {
            nextForcedRefresh = Instant.now().plusSeconds(refreshPeriod.getSeconds());
        }
    }

    /**
     * Updates the next refresh time for all stores using exponential backoff on failures.
     * Sets a minimum value until the next refresh. If a refresh interval has passed or is smaller than the calculated
     * backoff time, the refresh interval is set to the backoff time. This prevents excessive refresh attempts
     * during transient failures.
     * @param refreshInterval period between refresh checks
     * @param defaultMinBackoff minimum backoff duration between checks in seconds
     */
    void updateNextRefreshTime(Duration refreshInterval, Long defaultMinBackoff) {
        if (refreshInterval != null) {
            Instant newForcedRefresh = getNextRefreshCheck(nextForcedRefresh,
                clientRefreshAttempts, refreshInterval.getSeconds(), defaultMinBackoff);

            if (newForcedRefresh.compareTo(nextForcedRefresh) != 0) {
                clientRefreshAttempts += 1;
            }
            nextForcedRefresh = newForcedRefresh;
        }

        for (Entry<String, State> entry : state.entrySet()) {
            State storeState = entry.getValue();
            Instant newRefresh = getNextRefreshCheck(storeState.getNextRefreshCheck(),
                storeState.getRefreshAttempt(), (long) storeState.getRefreshInterval(), defaultMinBackoff);

            State updatedState;
            if (newRefresh.compareTo(storeState.getNextRefreshCheck()) != 0) {
                updatedState = new State(storeState.withIncrementedRefreshAttempt(), newRefresh);
            } else {
                updatedState = new State(storeState, newRefresh);
            }
            this.state.put(entry.getKey(), updatedState);
        }
    }

    /**
     * Calculates the amount of time to the next refresh, if a refresh fails. Takes current Refresh date into account
     * for watch keys. Used for checking client refresh-interval only.
     * @param nextRefreshCheck next refresh for the whole client
     * @param attempt refresh attempt for the client
     * @param interval the Refresh Interval
     * @param defaultMinBackoff min backoff between checks
     * @return new Refresh Date
     */
    private Instant getNextRefreshCheck(Instant nextRefreshCheck, int attempt, Long interval,
        Long defaultMinBackoff) {
        // The refresh interval is only updated if it is expired.
        if (!Instant.now().isAfter(nextRefreshCheck)) {
            return nextRefreshCheck;
        }

        int durationPeriod = Math.toIntExact(interval);

        Instant now = Instant.now();

        if (durationPeriod <= defaultMinBackoff) {
            return now.plusSeconds(interval);
        }

        return now.plusNanos(
            BackoffTimeCalculator.calculateBackoff(attempt));
    }

}
