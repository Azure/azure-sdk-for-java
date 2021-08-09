// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import static com.microsoft.azure.spring.cloud.config.Constants.CONFIGURATION_SUFFIX;
import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_STORE_WATCH_KEY;
import static com.microsoft.azure.spring.cloud.config.Constants.FEATURE_SUFFIX;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.DateUtils;
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
import com.microsoft.azure.spring.cloud.config.stores.ClientStore;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

@Component
public class AppConfigurationRefresh implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefresh.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private ApplicationEventPublisher publisher;

    private final List<ConfigStore> configStores;

    private final Map<String, List<String>> storeContextsMap;

    private final Duration delay;

    private final ClientStore clientStore;

    private Date lastCheckedTime;

    private String eventDataInfo;

    private final List<String> featureWatchKey = new ArrayList<>();

    public AppConfigurationRefresh(AppConfigurationProperties properties, Map<String, List<String>> storeContextsMap,
            ClientStore clientStore) {
        this.configStores = properties.getStores();
        this.storeContextsMap = storeContextsMap;
        this.delay = properties.getCacheExpiration();
        this.lastCheckedTime = new Date();
        this.clientStore = clientStore;
        this.eventDataInfo = "";
        featureWatchKey.add(FEATURE_STORE_WATCH_KEY);
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
        return new AsyncResult<>(refreshStores());
    }

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed.
     * If any store has a value that needs to be updated a refresh event is called after
     * every store is checked.
     * @return If a refresh event is called.
     */
    private boolean refreshStores() {
        boolean willRefresh = false;
        if (running.compareAndSet(false, true)) {
            try {
                Date notCachedTime = null;

                // LastCheckedTime isn't sent until refresh is run once, this forces a
                // eTag set on startup
                if (lastCheckedTime != null) {
                    notCachedTime = DateUtils.addSeconds(lastCheckedTime, Math.toIntExact(delay.getSeconds()));
                }
                Date date = new Date();
                if (notCachedTime == null || date.after(notCachedTime)) {
                    for (ConfigStore configStore : configStores) {
                        if (StateHolder.getLoadState(configStore.getEndpoint())) {
                            willRefresh = refresh_configurations(configStore) || willRefresh;
                            // Refresh Feature Flags
                            willRefresh = refreshFeatureFlag(configStore) || willRefresh;
                        } else {
                            LOGGER.debug("Skipping refresh check for " + configStore.getEndpoint()
                                    + ". The store failed to load on startup.");
                        }
                    }
                    // Resetting last Checked date to now.
                    lastCheckedTime = new Date();
                }
                if (willRefresh) {
                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    if (eventDataInfo.equals("*")) {
                        LOGGER.info("Configuration Refresh event triggered by store modification.");
                    } else {
                        LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);
                    }
                    RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                }
            } finally {
                running.set(false);
            }
        }
        return willRefresh;
    }

    /**
     * Checks un-cached items for etag changes. If they have changed a RefreshEventData is
     * published.
     *
     * @param store the {@code store} for which to composite watched key names
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private boolean refresh_configurations(ConfigStore store) {
        for (String context : storeContextsMap.get(store.getEndpoint())) {
            // Checking every Profile
            String storeNameWithSuffix = store.getEndpoint() + CONFIGURATION_SUFFIX + "_" + context;
            String watchedKeyName = clientStore.watchedKeyNames(store, context);

            if (checkETagChange(store, storeNameWithSuffix, watchedKeyName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks un-cached items for etag changes for feature flags. If they have changed a
     * RefreshEventData is published.
     *
     * @param store the {@code store} for which to composite watched key names
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private boolean refreshFeatureFlag(ConfigStore store) {
        String storeNameWithSuffix = store.getEndpoint() + FEATURE_SUFFIX;
        return checkETagChange(store, storeNameWithSuffix, FEATURE_STORE_WATCH_KEY);
    }

    private boolean checkETagChange(ConfigStore store, String storeNameWithSuffix, String watchKey) {
        SettingSelector settingSelector = new SettingSelector().setKeyFilter(watchKey)
                .setLabelFilter("*");
        ConfigurationSetting revision = clientStore.getRevison(settingSelector, store.getEndpoint());

        String etag = null;
        // If there is no result, etag will be considered empty.
        // A refresh will trigger once the selector returns a value.
        if (revision != null) {
            etag = revision.getETag();
        }

        if (StateHolder.getEtagState(storeNameWithSuffix) == null) {
            // On startup there was no Configurations, but now there is.
            if (etag != null) {
                LOGGER.info("The store " + store.getEndpoint() + " had no keys on startup, but now has keys to load.");
                return true;
            }
            return false;
        }

        if (etag != null && !etag.equals(StateHolder.getEtagState(storeNameWithSuffix).getETag())) {
            LOGGER.trace("Some keys in store [{}] matching [{}] is updated, will send refresh event.",
                    store.getEndpoint(), watchKey);
            if (this.eventDataInfo.isEmpty()) {
                this.eventDataInfo = watchKey;
            } else {
                this.eventDataInfo += ", " + watchKey;
            }

            // Don't need to refresh here will be done in Property Source
            return true;
        }
        return false;
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh
     * is required.
     */
    static class RefreshEventData {
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
