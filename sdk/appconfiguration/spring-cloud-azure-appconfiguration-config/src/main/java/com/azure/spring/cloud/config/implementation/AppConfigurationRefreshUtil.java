// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.implementation.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.config.implementation.properties.FeatureFlagKeyValueSelector;
import com.azure.spring.cloud.config.implementation.properties.FeatureFlagStore;

class AppConfigurationRefreshUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPullRefresh.class);

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    static RefreshEventData refreshStoresCheck(AppConfigurationProviderProperties appProperties,
        AppConfigurationReplicaClientFactory clientFactory, List<ConfigStore> configStores, Duration refreshInterval,
        List<String> profiles) {
        RefreshEventData eventData = new RefreshEventData();
        BaseAppConfigurationPolicy.setWatchRequests(true);

        try {
            if (refreshInterval != null && StateHolder.getNextForcedRefresh() != null
                && Instant.now().isAfter(StateHolder.getNextForcedRefresh())) {
                String eventDataInfo = "Minimum refresh period reached. Refreshing configurations.";

                LOGGER.info(eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }

            for (ConfigStore configStore : configStores) {
                if (configStore.isEnabled()) {
                    // For safety reset current used replica.
                    clientFactory.setCurrentConfigStoreClient(configStore.getEndpoint(), configStore.getEndpoint());

                    String originEndpoint = configStore.getEndpoint();
                    AppConfigurationStoreMonitoring monitor = configStore.getMonitoring();

                    List<AppConfigurationReplicaClient> clients = clientFactory.getAvailableClients(originEndpoint);

                    if (monitor.isEnabled() && StateHolder.getLoadState(originEndpoint)) {
                        for (AppConfigurationReplicaClient client : clients) {
                            try {
                                refreshWithTime(client, StateHolder.getState(originEndpoint), originEndpoint,
                                    monitor.getRefreshInterval(), eventData);
                                if (eventData.getDoRefresh()) {
                                    clientFactory.setCurrentConfigStoreClient(configStore.getEndpoint(),
                                        client.getEndpoint());
                                    return eventData;
                                }
                                // If check didn't throw an error other clients don't need to be checked.
                                break;
                            } catch (AppConfigurationStatusException e) {
                                LOGGER.warn("Failed attempting to connect to " + client.getEndpoint()
                                    + " durring refresh check.");

                                clientFactory.backoffClientClient(configStore.getEndpoint(), client.getEndpoint());
                                continue;
                            }
                        }
                    } else {
                        LOGGER.debug("Skipping configuration refresh check for " + originEndpoint);
                    }

                    FeatureFlagStore featureStore = configStore.getFeatureFlags();

                    if (featureStore.getEnabled() && StateHolder.getLoadStateFeatureFlag(originEndpoint)) {
                        for (AppConfigurationReplicaClient client : clients) {
                            try {
                                refreshWithTimeFeatureFlags(client, configStore.getFeatureFlags(),
                                    StateHolder.getStateFeatureFlag(originEndpoint),
                                    monitor.getFeatureFlagRefreshInterval(),
                                    eventData, profiles);
                                if (eventData.getDoRefresh()) {
                                    clientFactory.setCurrentConfigStoreClient(configStore.getEndpoint(),
                                        client.getEndpoint());
                                    return eventData;
                                }
                                // If check didn't throw an error other clients don't need to be checked.
                                break;
                            } catch (AppConfigurationStatusException e) {
                                LOGGER.warn("Failed attempting to connect to " + client.getEndpoint()
                                    + " durring refresh check.");

                                clientFactory.backoffClientClient(configStore.getEndpoint(), client.getEndpoint());
                                continue;
                            }
                        }
                    } else {
                        LOGGER.debug("Skipping feature flag refresh check for " + originEndpoint);
                    }
                }
            }
        } catch (Exception e) {
            // The next refresh will happen sooner if refresh interval is expired.
            StateHolder.getCurrentState().updateNextRefreshTime(refreshInterval, appProperties);
            throw e;
        }
        return eventData;
    }

    static boolean checkStoreAfterRefreshFailed(ConfigStore configStore, AppConfigurationReplicaClient client,
        AppConfigurationReplicaClientFactory clientFactory, List<String> profiles) {
        return refreshStoreCheck(client, clientFactory)
            || refreshStoreFeatureFlagCheck(configStore, client, clientFactory, profiles);
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * @param configStores
     * @param clientFactory
     * @return
     */
    private static boolean refreshStoreCheck(AppConfigurationReplicaClient client,
        AppConfigurationReplicaClientFactory clientFactory) {
        RefreshEventData eventData = new RefreshEventData();
        String endpoint = client.getEndpoint();
        String originEndpoint = clientFactory.findOriginForEndpoint(endpoint);
        if (StateHolder.getLoadState(originEndpoint)) {
            refreshWithoutTime(client, StateHolder.getState(originEndpoint).getWatchKeys(), eventData);
        }
        return eventData.getDoRefresh();
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * @param configStores
     * @param clientFactory
     * @return
     */
    private static boolean refreshStoreFeatureFlagCheck(ConfigStore configStore, AppConfigurationReplicaClient client,
        AppConfigurationReplicaClientFactory clientFactory, List<String> profiles) {
        RefreshEventData eventData = new RefreshEventData();
        String endpoint = client.getEndpoint();

        if (configStore.getFeatureFlags().getEnabled() && StateHolder.getLoadStateFeatureFlag(endpoint)) {
            refreshWithoutTimeFeatureFlags(client, configStore.getFeatureFlags(),
                StateHolder.getStateFeatureFlag(endpoint).getWatchKeys(), eventData, profiles);
        } else {
            LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
        }
        return eventData.getDoRefresh();
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param state The refresh state of the endpoint being checked.
     * @param endpoint The App Config Endpoint being checked for refresh.
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private static void refreshWithTime(AppConfigurationReplicaClient client, State state, String endpoint,
        Duration refreshInterval, RefreshEventData eventData) throws AppConfigurationStatusException {
        if (Instant.now().isAfter(state.getNextRefreshCheck())) {

            refreshWithoutTime(client, state.getWatchKeys(), eventData);

            if (eventData.getDoRefresh()) {
                // Just need to reset refreshInterval, if a refresh was triggered it will updated after loading the new
                // configurations.
                StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
            }
        }
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param state The refresh state of the endpoint being checked.
     * @param endpoint The App Config Endpoint being checked for refresh.
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private static void refreshWithoutTime(AppConfigurationReplicaClient client,
        List<ConfigurationSetting> watchKeys, RefreshEventData eventData) throws AppConfigurationStatusException {
        for (ConfigurationSetting watchKey : watchKeys) {
            ConfigurationSetting watchedKey = client.getWatchKey(watchKey.getKey(), watchKey.getLabel());

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

    private static void refreshWithTimeFeatureFlags(AppConfigurationReplicaClient client,
        FeatureFlagStore featureStore, State state, Duration refreshInterval, RefreshEventData eventData,
        List<String> profiles)
        throws AppConfigurationStatusException {
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {

            for (FeatureFlagKeyValueSelector watchKey : featureStore.getSelects()) {
                SettingSelector selector = new SettingSelector().setKeyFilter(watchKey.getKeyFilter())
                    .setLabelFilter(watchKey.getLabelFilterText(profiles));
                PagedIterable<ConfigurationSetting> currentKeys = client.listSettings(selector);

                int watchedKeySize = 0;

                keyCheck: for (ConfigurationSetting currentKey : currentKeys) {

                    watchedKeySize += 1;
                    for (ConfigurationSetting watchFlag : state.getWatchKeys()) {

                        // If there is no result, etag will be considered empty.
                        // A refresh will trigger once the selector returns a value.
                        if (watchFlag != null && watchFlag.getKey().equals(currentKey.getKey())) {
                            checkETag(watchFlag, currentKey, client.getEndpoint(), eventData);
                            if (eventData.getDoRefresh()) {
                                break keyCheck;

                            }
                        } else {
                            break keyCheck;
                        }

                    }
                }

                if (watchedKeySize != state.getWatchKeys().size()) {
                    String eventDataInfo = ".appconfig.featureflag/*";

                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                    eventData.setMessage(eventDataInfo);
                }
            }

            // Just need to reset refreshInterval, if a refresh was triggered it will updated after loading the new
            // configurations.
            StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
        }
    }

    private static void refreshWithoutTimeFeatureFlags(AppConfigurationReplicaClient client,
        FeatureFlagStore featureStore, List<ConfigurationSetting> watchKeys, RefreshEventData eventData,
        List<String> profiles)
        throws AppConfigurationStatusException {

        for (FeatureFlagKeyValueSelector watchKey : featureStore.getSelects()) {
            SettingSelector selector = new SettingSelector().setKeyFilter(watchKey.getKeyFilter())
                .setLabelFilter(watchKey.getLabelFilterText(profiles));
            PagedIterable<ConfigurationSetting> currentTriggerConfigurations = client.listSettings(selector);

            int watchedKeySize = 0;

            for (ConfigurationSetting currentTriggerConfiguration : currentTriggerConfigurations) {
                watchedKeySize += 1;
                for (ConfigurationSetting watchFlag : watchKeys) {

                    // If there is no result, etag will be considered empty.
                    // A refresh will trigger once the selector returns a value.
                    if (watchFlag != null && watchFlag.getKey().equals(currentTriggerConfiguration.getKey())) {
                        checkETag(watchFlag, currentTriggerConfiguration, client.getEndpoint(), eventData);
                        if (eventData.getDoRefresh()) {
                            return;
                        }
                    } else {
                        break;
                    }

                }
            }

            if (watchedKeySize != watchKeys.size()) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }
        }

        return;
    }

    private static void checkETag(ConfigurationSetting watchSetting, ConfigurationSetting currentTriggerConfiguration,
        String endpoint, RefreshEventData eventData) {
        if (currentTriggerConfiguration == null) {
            return;
        }

        LOGGER.debug(watchSetting.getETag(), " - ", currentTriggerConfiguration.getETag());
        if (watchSetting.getETag() != null && !watchSetting.getETag().equals(currentTriggerConfiguration.getETag())) {
            LOGGER.trace(
                "Some keys in store [{}] matching the key [{}] and label [{}] is updated, "
                    + "will send refresh event.",
                endpoint, watchSetting.getKey(), watchSetting.getLabel());

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
            this.message = String.format(MSG_TEMPLATE, prefix);
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
