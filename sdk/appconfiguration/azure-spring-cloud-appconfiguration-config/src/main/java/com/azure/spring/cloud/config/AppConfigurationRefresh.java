// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;
import com.azure.spring.cloud.config.stores.ClientStore;

/**
 * Enables checking of Configuration updates.
 *
 */
@Component
public class AppConfigurationRefresh implements ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefresh.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final List<ConfigStore> configStores;
    private ApplicationEventPublisher publisher;
    private final ClientStore clientStore;

    private String eventDataInfo;

    public AppConfigurationRefresh(AppConfigurationProperties properties, ClientStore clientStore) {
        this.configStores = properties.getStores();
        this.clientStore = clientStore;
        this.eventDataInfo = "";
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    /**
     * Checks configurations to see if configurations should be reloaded. If the refresh interval has passed and a trigger has been updated configuration are reloaded.
     *
     * @return Future with a boolean of if a RefreshEvent was published. If refreshConfigurations is currently being run
     * elsewhere this method will return right away as <b>false</b>.
     */
    @Async
    public Future<Boolean> refreshConfigurations() {
        return new AsyncResult<>(refreshStores());
    }

    public void expireRefreshInterval(String endpoint) {
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint)) {
                LOGGER.debug("Expiring refresh interval for " + configStore.getEndpoint());
                StateHolder.expireState(configStore.getEndpoint());
                break;
            }
        }
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    private boolean refreshStores() {
        boolean didRefresh = false;
        if (running.compareAndSet(false, true)) {
            try {
                for (ConfigStore configStore : configStores) {
                    if (configStore.isEnabled()) {
                        String endpoint = configStore.getEndpoint();
                        AppConfigurationStoreMonitoring monitor = configStore.getMonitoring();

                        if (StateHolder.getLoadState(endpoint) && monitor.isEnabled()
                            && refresh(StateHolder.getState(endpoint), endpoint, monitor.getRefreshInterval())) {
                            didRefresh = true;
                            break;
                        } else {
                            LOGGER.debug("Skipping configuration refresh check for " + endpoint);
                        }

                        FeatureFlagStore featureStore = configStore.getFeatureFlags();

                        if (featureStore.getEnabled() && StateHolder.getLoadStateFeatureFlag(endpoint) && refresh(
                            StateHolder.getStateFeatureFlag(endpoint), endpoint, monitor.getFeatureFlagRefreshInterval())) {
                            didRefresh = true;
                            break;
                        } else {
                            LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
                        }
                    }
                }
            } finally {
                running.set(false);
            }
        }
        return didRefresh;

    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param state The refresh state of the endpoint being checked.
     * @param endpoint The App Config Endpoint being checked for refresh.
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private boolean refresh(State state, String endpoint, Duration refreshInterval) {
        Date date = new Date();
        if (date.after(state.getNextRefreshCheck())) {
            for (ConfigurationSetting watchKey : state.getWatchKeys()) {
                SettingSelector settingSelector = new SettingSelector().setKeyFilter(watchKey.getKey())
                    .setLabelFilter(watchKey.getLabel());

                ConfigurationSetting watchedKey = clientStore.getWatchKey(settingSelector, endpoint);

                String etag = null;
                // If there is no result, etag will be considered empty.
                // A refresh will trigger once the selector returns a value.
                if (watchedKey != null) {
                    etag = watchedKey.getETag();
                }

                LOGGER.debug(etag + " - " + watchKey.getETag());
                if (etag != null && !etag.equals(watchKey.getETag())) {
                    LOGGER.trace(
                        "Some keys in store [{}] matching the key [{}] and label [{}] is updated, "
                            + "will send refresh event.",
                        endpoint, watchKey.getKey(), watchKey.getLabel());

                    this.eventDataInfo = watchKey.getKey();

                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                    RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                    return true;
                }
            }
            StateHolder.setState(endpoint, state.getWatchKeys(), refreshInterval);
        }

        return false;
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh is required.
     */
    static class RefreshEventData {

        private static final String MSG_TEMPLATE = "Some keys matching %s has been updated since last check.";

        private final String message;

        RefreshEventData(String prefix) {
            this.message = String.format(MSG_TEMPLATE, prefix);
        }

        public String getMessage() {
            return this.message;
        }
    }
}
