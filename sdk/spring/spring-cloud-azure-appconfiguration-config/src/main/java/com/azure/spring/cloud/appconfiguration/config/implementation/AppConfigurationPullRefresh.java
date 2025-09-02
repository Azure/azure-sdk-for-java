// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;
import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;
import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;

import reactor.core.publisher.Mono;

/**
 * Component responsible for checking Azure App Configuration for updates and triggering refresh events.
 */
@Component
public class AppConfigurationPullRefresh implements AppConfigurationRefresh {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPullRefresh.class);

    /**
     * Flag to prevent concurrent refresh operations.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Publisher for Spring refresh events.
     */
    private ApplicationEventPublisher publisher;
    private final Long defaultMinBackoff = (long) 30;

    /**
     * Default minimum backoff duration in seconds when refresh operations fail.
     */
    private static final Long DEFAULT_MIN_BACKOFF_SECONDS = 30L;

    /**
     * Factory for creating App Configuration replica clients.
     */
    private final AppConfigurationReplicaClientFactory clientFactory;

    /**
     * Time interval between configuration refresh checks.
     */
    private final Duration refreshInterval;

    /**
     * Component for replica lookup and failover functionality.
     */
    private final ReplicaLookUp replicaLookUp;

    /**
     * Utility component for refresh operations.
     */
    private final AppConfigurationRefreshUtil refreshUtils;

    /**
     * Creates a new AppConfigurationPullRefresh component.
     *
     * @param clientFactory factory for creating App Configuration clients to connect to stores
     * @param refreshInterval time duration between refresh interval checks
     * @param replicaLookUp component for handling replica lookup and failover
     * @param refreshUtils utility component for refresh operations
     */
    public AppConfigurationPullRefresh(AppConfigurationReplicaClientFactory clientFactory, Duration refreshInterval,
        ReplicaLookUp replicaLookUp, AppConfigurationRefreshUtil refreshUtils) {
        this.refreshInterval = refreshInterval;
        this.clientFactory = clientFactory;
        this.replicaLookUp = replicaLookUp;
        this.refreshUtils = refreshUtils;
    }

    /**
     * Sets the Spring application event publisher for publishing refresh events.
     * 
     * @param applicationEventPublisher the Spring event publisher to use for refresh events
     */
    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * Checks configurations to see if they should be reloaded. If the refresh interval has passed and a trigger has
     * been updated, configurations are reloaded.
     * 
     * @return a Mono containing a boolean indicating if a RefreshEvent was published. Returns {@code false} if
     * refreshConfigurations is currently being executed elsewhere.
     */
    public Mono<Boolean> refreshConfigurations() {
        return Mono.just(refreshStores());
    }

    /**
     * Soft expires refresh interval. Sets amount of time to next refresh to be a random value between 0 and 15 seconds,
     * unless that value is less than the amount of time to the next refresh check.
     * 
     * @param endpoint the Config Store endpoint to expire refresh interval on
     * @param syncToken the syncToken to verify the latest changes are available on pull
     */
    public void expireRefreshInterval(String endpoint, String syncToken) {
        LOGGER.debug("Expiring refresh interval for " + endpoint);

        String originEndpoint = clientFactory.findOriginForEndpoint(endpoint);

        clientFactory.updateSyncToken(originEndpoint, endpoint, syncToken);

        StateHolder.getCurrentState().expireState(originEndpoint);
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated, a refresh event is called after every store is checked.
     *
     * @return true if a refresh event is published, false otherwise
     */
    private boolean refreshStores() {
        if (running.compareAndSet(false, true)) {
            try {
                RefreshEventData eventData = refreshUtils.refreshStoresCheck(clientFactory,
                    refreshInterval, DEFAULT_MIN_BACKOFF_SECONDS, replicaLookUp);
                if (eventData.getDoRefresh()) {
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                    return true;
                }
            } catch (Exception e) {
                LOGGER.warn("Error occurred during configuration refresh, will retry at next interval", e);
                // The next refresh will happen sooner if refresh interval is expired.
                StateHolder.getCurrentState().updateNextRefreshTime(refreshInterval, DEFAULT_MIN_BACKOFF_SECONDS);
                throw e;
            } finally {
                running.set(false);
            }
        }
        return false;
    }

    /**
     * Gets the health status of all configured App Configuration stores.
     * 
     * @return a map containing the health status of each store, keyed by store identifier
     */
    @Override
    public Map<String, AppConfigurationStoreHealth> getAppConfigurationStoresHealth() {
        return clientFactory.getHealth();
    }

}
