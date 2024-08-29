// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.exception.HttpResponseException;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlagState;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

public class AppConfigurationRefreshUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPullRefresh.class);

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    RefreshEventData refreshStoresCheck(AppConfigurationReplicaClientFactory clientFactory, Duration refreshInterval,
        Long defaultMinBackoff, ReplicaLookUp replicaLookUp) {
        RefreshEventData eventData = new RefreshEventData();

        try {
            if (refreshInterval != null && StateHolder.getNextForcedRefresh() != null
                && Instant.now().isAfter(StateHolder.getNextForcedRefresh())) {
                String eventDataInfo = "Minimum refresh period reached. Refreshing configurations.";

                LOGGER.info(eventDataInfo);

                eventData.setFullMessage(eventDataInfo);
                return eventData;
            }

            for (Entry<String, ConnectionManager> entry : clientFactory.getConnections().entrySet()) {
                String originEndpoint = entry.getKey();
                ConnectionManager connection = entry.getValue();
                // For safety reset current used replica.
                clientFactory.setCurrentConfigStoreClient(originEndpoint, originEndpoint);

                AppConfigurationStoreMonitoring monitor = connection.getMonitoring();

                List<AppConfigurationReplicaClient> clients = clientFactory.getAvailableClients(originEndpoint);

                if (monitor.isEnabled() && StateHolder.getLoadState(originEndpoint)) {
                    for (AppConfigurationReplicaClient client : clients) {
                        try {
                            refreshWithTime(client, StateHolder.getState(originEndpoint), monitor.getRefreshInterval(),
                                eventData, replicaLookUp);
                            if (eventData.getDoRefresh()) {
                                clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                return eventData;
                            }
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (HttpResponseException e) {
                            LOGGER.warn(
                                "Failed attempting to connect to " + client.getEndpoint() + " during refresh check.");

                            clientFactory.backoffClientClient(originEndpoint, client.getEndpoint());
                        }
                    }
                } else {
                    LOGGER.debug("Skipping configuration refresh check for " + originEndpoint);
                }

                FeatureFlagStore featureStore = connection.getFeatureFlagStore();

                if (featureStore.getEnabled() && StateHolder.getStateFeatureFlag(originEndpoint) != null) {
                    for (AppConfigurationReplicaClient client : clients) {
                        try {
                            refreshWithTimeFeatureFlags(client, StateHolder.getStateFeatureFlag(originEndpoint),
                                monitor.getFeatureFlagRefreshInterval(), eventData, replicaLookUp);
                            if (eventData.getDoRefresh()) {
                                clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                return eventData;
                            }
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (HttpResponseException e) {
                            LOGGER.warn(
                                "Failed attempting to connect to " + client.getEndpoint() + " during refresh check.");

                            clientFactory.backoffClientClient(originEndpoint, client.getEndpoint());
                        }
                    }
                } else {
                    LOGGER.debug("Skipping feature flag refresh check for " + originEndpoint);
                }

            }
        } catch (Exception e) {
            // The next refresh will happen sooner if refresh interval is expired.
            StateHolder.getCurrentState().updateNextRefreshTime(refreshInterval, defaultMinBackoff);
            throw e;
        }
        return eventData;
    }

    static boolean checkStoreAfterRefreshFailed(AppConfigurationReplicaClient client,
        AppConfigurationReplicaClientFactory clientFactory, FeatureFlagStore featureStore) {
        return refreshStoreCheck(client, clientFactory.findOriginForEndpoint(client.getEndpoint()))
            || refreshStoreFeatureFlagCheck(featureStore, client);
    }

    /**
     * This is for a <b>refresh fail only</b>.
     *
     * @param client Client checking for refresh
     * @param originEndpoint config store origin endpoint
     * @return A refresh should be triggered.
     */
    private static boolean refreshStoreCheck(AppConfigurationReplicaClient client, String originEndpoint) {
        RefreshEventData eventData = new RefreshEventData();
        if (StateHolder.getLoadState(originEndpoint)) {
            refreshWithoutTime(client, StateHolder.getState(originEndpoint).getWatchKeys(), eventData);
        }
        return eventData.getDoRefresh();
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * 
     * @param featureStore Feature info for the store
     * @param client Client checking for refresh
     * @return true if a refresh should be triggered.
     */
    private static boolean refreshStoreFeatureFlagCheck(FeatureFlagStore featureStore,
        AppConfigurationReplicaClient client) {
        RefreshEventData eventData = new RefreshEventData();
        String endpoint = client.getEndpoint();

        if (featureStore.getEnabled() && StateHolder.getStateFeatureFlag(endpoint) != null) {
            refreshWithoutTimeFeatureFlags(client, StateHolder.getStateFeatureFlag(endpoint), eventData);
        } else {
            LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
        }
        return eventData.getDoRefresh();
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param state The refresh state of the endpoint being checked.
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @param eventData Info for this refresh event.
     */
    private static void refreshWithTime(AppConfigurationReplicaClient client, State state, Duration refreshInterval,
        RefreshEventData eventData, ReplicaLookUp replicaLookUp) throws AppConfigurationStatusException {
        if (Instant.now().isAfter(state.getNextRefreshCheck())) {
            replicaLookUp.updateAutoFailoverEndpoints();
            refreshWithoutTime(client, state.getWatchKeys(), eventData);

            StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
        }
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param client Client checking for refresh
     * @param watchKeys Watch keys for the store.
     * @param eventData Refresh event info
     */
    private static void refreshWithoutTime(AppConfigurationReplicaClient client, List<ConfigurationSetting> watchKeys,
        RefreshEventData eventData) throws AppConfigurationStatusException {
        for (ConfigurationSetting watchKey : watchKeys) {
            ConfigurationSetting watchedKey = client.getWatchKey(watchKey.getKey(), watchKey.getLabel(), true);

            // If there is no result, etag will be considered empty.
            // A refresh will trigger once the selector returns a value.
            if (watchedKey != null) {
                checkETag(watchKey, watchedKey, client.getEndpoint(), eventData);
                if (eventData.getDoRefresh()) {
                    break;
                }
            }
        }
    }

    private static void refreshWithTimeFeatureFlags(AppConfigurationReplicaClient client, FeatureFlagState state,
        Duration refreshInterval, RefreshEventData eventData, ReplicaLookUp replicaLookUp)
        throws AppConfigurationStatusException {
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {
            replicaLookUp.updateAutoFailoverEndpoints();

            for (FeatureFlags featureFlags : state.getWatchKeys()) {

                if (client.checkWatchKeys(featureFlags.getSettingSelector(), true)) {
                    String eventDataInfo = ".appconfig.featureflag/*";

                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                    eventData.setMessage(eventDataInfo);
                    return;
                }

            }

            StateHolder.getCurrentState().updateFeatureFlagStateRefresh(state, refreshInterval);
        }
    }

    private static void refreshWithoutTimeFeatureFlags(AppConfigurationReplicaClient client, FeatureFlagState watchKeys,
        RefreshEventData eventData) throws AppConfigurationStatusException {

        for (FeatureFlags featureFlags : watchKeys.getWatchKeys()) {

            if (client.checkWatchKeys(featureFlags.getSettingSelector(), true)) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }

        }
    }

    private static void checkETag(ConfigurationSetting watchSetting, ConfigurationSetting currentTriggerConfiguration,
        String endpoint, RefreshEventData eventData) {
        if (currentTriggerConfiguration == null) {
            return;
        }

        LOGGER.debug(watchSetting.getETag(), " - ", currentTriggerConfiguration.getETag());
        if (watchSetting.getETag() != null && !watchSetting.getETag().equals(currentTriggerConfiguration.getETag())) {
            LOGGER.trace("Some keys in store [{}] matching the key [{}] and label [{}] is updated, "
                + "will send refresh event.", endpoint, watchSetting.getKey(), watchSetting.getLabel());

            String eventDataInfo = watchSetting.getKey();

            // Only one refresh Event needs to be call to update all of the
            // stores, not one for each.
            LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);
            eventData.setMessage(eventDataInfo);
        }
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh is required.
     */
    static class RefreshEventData {

        private static final String MSG_TEMPLATE = "Some keys matching %s has been updated since last check.";

        private String message;

        private boolean doRefresh = false;

        RefreshEventData() {
            this.message = "";
        }

        RefreshEventData setMessage(String prefix) {
            setFullMessage(String.format(MSG_TEMPLATE, prefix));
            return this;
        }

        RefreshEventData setFullMessage(String message) {
            this.message = message;
            this.doRefresh = true;
            return this;
        }

        public String getMessage() {
            return this.message;
        }

        public boolean getDoRefresh() {
            return doRefresh;
        }
    }
}
