// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.azure.spring.cloud.config.AppConfigurationRefresh;
import com.azure.spring.cloud.config.ClientFactory;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.implementation.AppConfigurationRefreshUtil.RefreshEventData;
import com.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Enables checking of Configuration updates.
 */
@Component
public class AppConfigurationPullRefresh implements AppConfigurationRefresh {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPullRefresh.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final List<ConfigStore> configStores;

    private ApplicationEventPublisher publisher;

    private final AppConfigurationProviderProperties appProperties;

    private final ClientFactory clientFactory;

    private final Duration refreshInterval;

    /**
     * Component used for checking for and triggering configuration refreshes.
     *
     * @param properties Client properties to check against.
     * @param appProperties Library properties for configuring backoff
     * @param clientStore Clients stores used to connect to App Configuration.
     */
    public AppConfigurationPullRefresh(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ClientFactory clientFactory) {
        this.appProperties = appProperties;
        this.configStores = properties.getStores();
        this.refreshInterval = properties.getRefreshInterval();
        this.clientFactory = clientFactory;

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * Checks configurations to see if configurations should be reloaded. If the refresh interval has passed and a
     * trigger has been updated configuration are reloaded.
     *
     * @return Future with a boolean of if a RefreshEvent was published. If refreshConfigurations is currently being run
     * elsewhere this method will return right away as <b>false</b>.
     */
    @Async
    public Future<Boolean> refreshConfigurations() {
        return new AsyncResult<>(refreshStores());
    }

    /**
     * Soft expires refresh interval. Sets amount of time to next refresh to be a random value between 0 and 15 seconds,
     * unless value is less than the amount of time to the next refresh check.
     * @param endpoint Config Store endpoint to expire refresh interval on.
     * @param syncToken syncToken to verify latest changes are available on pull
     */
    public void expireRefreshInterval(String endpoint, String syncToken) {
        LOGGER.debug("Expiring refresh interval for " + endpoint);

        String originEndpoint = clientFactory.findOriginForEndpoint(endpoint);

        // Sync token can only be used if no replicas are being used.
        if (clientFactory.hasReplicas(endpoint)) {
            clientFactory.getAvailableClients(endpoint).get(0).updateSyncToken(syncToken);
        }

        StateHolder.expireState(originEndpoint);
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    private boolean refreshStores() {
        RefreshEventData eventData = new RefreshEventData();
        if (running.compareAndSet(false, true)) {
            BaseAppConfigurationPolicy.setWatchRequests(true);
            try {
                AppConfigurationRefreshUtil.refreshStoresCheck(appProperties, clientFactory, configStores,
                    refreshInterval, eventData);
                if (eventData.getDoRefresh()) {
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                }
            } catch (Exception e) {
                // The next refresh will happen sooner if refresh interval is expired.
                StateHolder.updateNextRefreshTime(refreshInterval, appProperties);
                throw e;
            } finally {
                running.set(false);
            }
        }
        return eventData.getDoRefresh();
    }

    @Override
    public Map<String, AppConfigurationStoreHealth> getAppConfigurationStoresHealth() {
        return clientFactory.getHealth();
    }

}
