/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

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
import com.microsoft.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.microsoft.azure.spring.cloud.config.properties.ConfigStore;
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;

@Component
public class AppConfigurationRefresh implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefresh.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ApplicationEventPublisher publisher;

    private final List<ConfigStore> configStores;

    private ClientStore clientStore;

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
     * Checks configurations to see if they are no longer cached. If they are no longer
     * cached they are updated.
     * 
     * @return Future with a boolean of if a RefreshEvent was published. If
     * refreshConfigurations is currently being run elsewhere this method will return
     * right away as <b>false</b>.
     */
    @Async
    public Future<Boolean> refreshConfigurations() {
        return new AsyncResult<Boolean>(refreshStores());
    }

    public void resetCache(String endpoint) {
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint)) {
                LOGGER.debug("Expiring Cache for " + configStore.getEndpoint());
                StateHolder.expireState(configStore.getEndpoint());
                break;
            }
        }
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed.
     * If any store has a value that needs to be updated a refresh event is called after
     * every store is checked.
     * @return If a refresh event is called.
     */
    private boolean refreshStores() {
        boolean didRefresh = false;
        if (running.compareAndSet(false, true)) {
            try {
                for (ConfigStore configStore : configStores) {
                    if (StateHolder.getLoadState(configStore.getEndpoint()) && configStore.getMonitoring().isEnabled()
                            && refresh(configStore)) {
                        // Only one refresh Event needs to be call to update all of the
                        // stores, not one for each.
                        if (eventDataInfo.equals("*")) {
                            LOGGER.info("Configuration Refresh event triggered by store modification.");
                        } else {
                            LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);
                        }
                        RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                        publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                        didRefresh = true;
                        break;

                    } else {
                        LOGGER.debug("Skipping refresh check for " + configStore.getEndpoint());
                    }
                }
            } finally {
                running.set(false);
            }
        }
        return didRefresh;

    }

    /**
     * Checks un-cached items for etag changes. If they have changed a RefreshEventData is
     * published.
     * 
     * @param store the {@code store} for which to composite watched key names
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private boolean refresh(ConfigStore store) {
        State state = StateHolder.getState(store.getEndpoint());

        Date date = new Date();
        if (date.after(state.getNotCachedTime())) {
            for (ConfigurationSetting watchKey : state.getWatchKeys()) {
                SettingSelector settingSelector = new SettingSelector().setKeyFilter(watchKey.getKey())
                        .setLabelFilter(watchKey.getLabel());

                ConfigurationSetting revision = clientStore.getRevison(settingSelector, store.getEndpoint());

                String etag = null;
                // If there is no result, etag will be considered empty.
                // A refresh will trigger once the selector returns a value.
                if (revision != null) {
                    etag = revision.getETag();
                }

                LOGGER.error(etag + " - " + watchKey.getETag());
                if (etag != null && !etag.equals(watchKey.getETag())) {
                    LOGGER.trace(
                            "Some keys in store [{}] matching the key [{}] and label [{}] is updated, " +
                                    "will send refresh event.",
                            store.getEndpoint(), watchKey.getKey(), watchKey.getLabel());

                    this.eventDataInfo = watchKey.toString();

                    // Don't need to refresh here will be done in Property Source
                    return true;
                }
            }
            StateHolder.setState(store.getEndpoint(), state.getWatchKeys(), store.getMonitoring());
        }

        return false;
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh
     * is required.
     */
    class RefreshEventData {
        private static final String MSG_TEMPLATE = "Some keys matching %s has been updated since last check.";

        private final String message;

        public RefreshEventData(String prefix) {
            this.message = String.format(MSG_TEMPLATE, prefix);
        }

        public String getMessage() {
            return this.message;
        }
    }
}
