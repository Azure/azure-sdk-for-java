// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Instant;
import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.CollectionMonitoring;

/**
 * Immutable representation of the refresh state for an Azure App Configuration store.
 * 
 * <p>Holds configuration watch keys, collection monitoring settings, refresh timing, and
 * attempt tracking for a single configuration store endpoint. All fields are final to ensure
 * thread-safety and immutability.</p>
 * 
 * <p>State changes are made by creating new instances rather than mutating existing ones,
 * following an immutable design pattern.</p>
 */
class State {

    /** Configuration settings used as watch keys to trigger refresh events. */
    private final List<ConfigurationSetting> watchKeys;

    /** Collection monitoring configurations that can trigger refresh events. */
    private final List<CollectionMonitoring> collectionWatchKeys;

    /** The next time this store should be checked for refresh. */
    private final Instant nextRefreshCheck;

    /** The endpoint URL of the configuration store. */
    private final String originEndpoint;

    /** Number of refresh attempts for exponential backoff calculation. */
    private final int refreshAttempt;

    /** The refresh interval in seconds. */
    private final int refreshInterval;

    /**
     * Creates a new State for configuration watch keys without collection monitoring.
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param refreshInterval refresh interval in seconds
     * @param originEndpoint the endpoint URL of the configuration store
     */
    State(List<ConfigurationSetting> watchKeys, int refreshInterval, String originEndpoint) {
        this(watchKeys, null, refreshInterval, originEndpoint);
    }

    /**
     * Creates a new State with both configuration watch keys and collection monitoring.
     * Sets the initial refresh attempt to 1 and calculates next refresh time from now.
     * @param watchKeys list of configuration watch keys that can trigger a refresh event
     * @param collectionWatchKeys list of collection monitoring configurations that can trigger a refresh event
     * @param refreshInterval refresh interval in seconds
     * @param originEndpoint the endpoint URL of the configuration store
     */
    State(List<ConfigurationSetting> watchKeys, List<CollectionMonitoring> collectionWatchKeys, int refreshInterval, String originEndpoint) {
        this(watchKeys, collectionWatchKeys, refreshInterval, originEndpoint, Instant.now().plusSeconds(refreshInterval), 1);
    }

    /**
     * Creates a new State from an existing state with an updated refresh time.
     * Preserves the current refresh attempt count.
     * @param oldState the existing State to copy from
     * @param newRefresh the new refresh time
     */
    State(State oldState, Instant newRefresh) {
        this(oldState, newRefresh, oldState.getRefreshAttempt());
    }

    /**
     * Creates a new State from an existing state with updated refresh time and attempt count.
     * Used when creating states with modified refresh attempts for backoff logic.
     * @param oldState the existing State to copy from
     * @param newRefresh the new refresh time
     * @param refreshAttempt the refresh attempt count
     */
    State(State oldState, Instant newRefresh, int refreshAttempt) {
        this(oldState.getWatchKeys(), oldState.getCollectionWatchKeys(), oldState.getRefreshInterval(),
            oldState.getOriginEndpoint(), newRefresh, refreshAttempt);
    }

    /**
     * Primary constructor that initializes all fields. All other constructors delegate to this one.
     * This constructor is private to enforce the use of the public factory-style constructors.
     * @param watchKeys list of configuration watch keys
     * @param collectionWatchKeys list of collection monitoring configurations (may be null)
     * @param refreshInterval refresh interval in seconds
     * @param originEndpoint the endpoint URL of the configuration store
     * @param nextRefreshCheck the next time to check for refresh
     * @param refreshAttempt the current refresh attempt count
     */
    private State(List<ConfigurationSetting> watchKeys, List<CollectionMonitoring> collectionWatchKeys,
                  int refreshInterval, String originEndpoint, Instant nextRefreshCheck, int refreshAttempt) {
        this.watchKeys = watchKeys;
        this.collectionWatchKeys = collectionWatchKeys;
        this.refreshInterval = refreshInterval;
        this.nextRefreshCheck = nextRefreshCheck;
        this.originEndpoint = originEndpoint;
        this.refreshAttempt = refreshAttempt;
    }

    /**
     * Gets the configuration settings used as watch keys for this store.
     * @return the list of configuration watch keys
     */
    public List<ConfigurationSetting> getWatchKeys() {
        return watchKeys;
    }

    /**
     * Gets the collection monitoring configurations for this store.
     * @return the list of collection monitoring configurations, or null if not configured
     */
    public List<CollectionMonitoring> getCollectionWatchKeys() {
        return collectionWatchKeys;
    }

    /**
     * Gets the next time this store should be checked for refresh.
     * @return the Instant of the next refresh check
     */
    public Instant getNextRefreshCheck() {
        return nextRefreshCheck;
    }

    /**
     * Gets the endpoint URL of the configuration store.
     * @return the origin endpoint
     */
    public String getOriginEndpoint() {
        return originEndpoint;
    }

    /**
     * Gets the number of refresh attempts. Used for exponential backoff calculation.
     * @return the refresh attempt count
     */
    public int getRefreshAttempt() {
        return refreshAttempt;
    }

    /**
     * Creates a new State with an incremented refresh attempt count.
     * This method follows the immutable pattern by returning a new instance rather than
     * modifying the current state. Used when a refresh fails to track attempts for backoff logic.
     * @return a new State instance with refreshAttempt incremented by 1
     */
    public State withIncrementedRefreshAttempt() {
        return new State(this, this.nextRefreshCheck, this.refreshAttempt + 1);
    }

    /**
     * Gets the refresh interval for this store.
     * @return the refresh interval in seconds
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

}
