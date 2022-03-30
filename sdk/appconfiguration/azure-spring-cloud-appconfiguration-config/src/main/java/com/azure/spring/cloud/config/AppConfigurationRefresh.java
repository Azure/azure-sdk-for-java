// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
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

    private final AppConfigurationProviderProperties appProperties;

    private final ClientStore clientStore;

    private Map<String, AppConfigurationStoreHealth> clientHealth;

    private String eventDataInfo;

    private final Duration refreshInterval;

    /**
     * Component used for checking for and triggering configuration refreshes.
     * 
     * @param properties Client properties to check against.
     * @param appProperties Library properties for configuring backoff
     * @param clientStore Clients stores used to connect to App Configuration.
     */
    public AppConfigurationRefresh(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ClientStore clientStore) {
        this.appProperties = appProperties;
        this.configStores = properties.getStores();
        this.refreshInterval = properties.getRefreshInterval();
        this.clientStore = clientStore;
        this.eventDataInfo = "";
        this.clientHealth = new HashMap<>();
        configStores.forEach(store -> {
            if (getStoreHealthState(store)) {
                this.clientHealth.put(store.getEndpoint(), AppConfigurationStoreHealth.UP);
            } else {
                this.clientHealth.put(store.getEndpoint(), AppConfigurationStoreHealth.NOT_LOADED);
            }
        });
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
        for (ConfigStore configStore : configStores) {
            if (configStore.getEndpoint().equals(endpoint)) {
                LOGGER.debug("Expiring refresh interval for " + configStore.getEndpoint());
                clientStore.updateSyncToken(endpoint, syncToken);
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
            BaseAppConfigurationPolicy.setWatchRequests(true);
            Map<String, AppConfigurationStoreHealth> clientHealthUpdate = new HashMap<>();
            configStores.forEach(store -> {
                if (getStoreHealthState(store)) {
                    clientHealthUpdate.put(store.getEndpoint(), AppConfigurationStoreHealth.DOWN);
                } else {
                    clientHealthUpdate.put(store.getEndpoint(), AppConfigurationStoreHealth.NOT_LOADED);
                }
            });
            try {
                if (refreshInterval != null && StateHolder.getNextForcedRefresh() != null
                    && Instant.now().isAfter(StateHolder.getNextForcedRefresh())) {
                    this.eventDataInfo = "Minimum refresh period reached. Refreshing configurations.";

                    LOGGER.info(eventDataInfo);

                    RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                    publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                    running.set(false);
                    return true;
                }
                for (ConfigStore configStore : configStores) {
                    if (configStore.isEnabled()) {
                        String endpoint = configStore.getEndpoint();
                        AppConfigurationStoreMonitoring monitor = configStore.getMonitoring();
                        if (StateHolder.getLoadState(endpoint)) {
                            if (monitor.isEnabled()
                                && refresh(StateHolder.getState(endpoint), endpoint, monitor.getRefreshInterval())) {
                                didRefresh = true;
                                break;
                            } else {
                                LOGGER.debug("Skipping configuration refresh check for " + endpoint);
                            }
                            clientHealthUpdate.put(configStore.getEndpoint(), AppConfigurationStoreHealth.UP);
                        }

                        FeatureFlagStore featureStore = configStore.getFeatureFlags();

                        if (StateHolder.getLoadStateFeatureFlag(endpoint)) {
                            if (featureStore.getEnabled()
                                && refreshFeatureFlags(configStore, StateHolder.getStateFeatureFlag(endpoint),
                                    endpoint, monitor.getFeatureFlagRefreshInterval())) {
                                didRefresh = true;
                                break;
                            } else {
                                LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
                            }
                            clientHealthUpdate.put(configStore.getEndpoint(), AppConfigurationStoreHealth.UP);
                        }
                    }
                }
            } catch (Exception e) {
                // The next refresh will happen sooner if refresh interval is expired.
                StateHolder.updateNextRefreshTime(refreshInterval, appProperties);
                throw e;
            } finally {
                running.set(false);
                clientHealth = clientHealthUpdate;
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
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {
            for (ConfigurationSetting watchKey : state.getWatchKeys()) {
                ConfigurationSetting watchedKey = clientStore.getWatchKey(watchKey.getKey(), watchKey.getLabel(),
                    endpoint);
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

            // Just need to reset refreshInterval, if a refresh was triggered it will updated after loading the new
            // configurations.
            StateHolder.setState(state, refreshInterval);
        }

        return false;
    }

    private boolean refreshFeatureFlags(ConfigStore configStore, State state, String endpoint,
        Duration refreshInterval) {
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {
            SettingSelector selector = new SettingSelector().setKeyFilter(configStore.getFeatureFlags().getKeyFilter())
                .setLabelFilter(configStore.getFeatureFlags().getLabelFilter());
            PagedIterable<ConfigurationSetting> currentKeys = clientStore.getFeatureFlagWatchKey(selector, endpoint);

            int watchedKeySize = 0;

            for (ConfigurationSetting currentKey : currentKeys) {
                watchedKeySize += 1;
                for (ConfigurationSetting watchFlag : state.getWatchKeys()) {

                    String etag = null;
                    // If there is no result, etag will be considered empty.
                    // A refresh will trigger once the selector returns a value.
                    if (watchFlag != null) {
                        etag = watchFlag.getETag();
                    } else {
                        break;
                    }

                    if (watchFlag.getKey().equals(currentKey.getKey())) {
                        LOGGER.debug(etag + " - " + currentKey.getETag());
                        if (etag != null && !etag.equals(currentKey.getETag())) {
                            LOGGER.trace(
                                "Some keys in store [{}] matching the key [{}] and label [{}] is updated, "
                                    + "will send refresh event.",
                                endpoint, watchFlag.getKey(), watchFlag.getLabel());

                            this.eventDataInfo = watchFlag.getKey();

                            // Only one refresh Event needs to be call to update all of the
                            // stores, not one for each.
                            LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                            RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                            publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                            return true;
                        }
                        break;

                    }
                }
            }

            if (watchedKeySize != state.getWatchKeys().size()) {
                this.eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                RefreshEventData eventData = new RefreshEventData(eventDataInfo);
                publisher.publishEvent(new RefreshEvent(this, eventData, eventData.getMessage()));
                return true;
            }

            // Just need to reset refreshInterval, if a refresh was triggered it will updated after loading the new
            // configurations.
            StateHolder.setState(state, refreshInterval);
        }

        return false;
    }

    /**
     * Gets latest Health connection info for refresh.
     * 
     * @return Map of String, endpoint, and Health information.
     */
    public Map<String, AppConfigurationStoreHealth> getAppConfigurationStoresHealth() {
        return this.clientHealth;
    }

    private Boolean getStoreHealthState(ConfigStore store) {
        return store.isEnabled() && (StateHolder.getLoadState(store.getEndpoint())
            || StateHolder.getLoadStateFeatureFlag(store.getEndpoint()));
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
