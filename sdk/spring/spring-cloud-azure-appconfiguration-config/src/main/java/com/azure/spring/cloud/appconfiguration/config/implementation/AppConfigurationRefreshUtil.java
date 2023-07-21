// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_PREFIX;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.SELECT_ALL_FEATURE_FLAGS;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.azure.core.http.rest.PagedFlux;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AppConfigurationRefreshUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPullRefresh.class);

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    static RefreshEventData refreshStoresCheck(AppConfigurationReplicaClientFactory clientFactory,
        Duration refreshInterval, List<String> profiles, Long defaultMinBackoff) {
        RefreshEventData eventData = new RefreshEventData();
        BaseAppConfigurationPolicy.setWatchRequests(true);

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
                                eventData);
                            if (eventData.getDoRefresh()) {
                                clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                return eventData;
                            }
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (AppConfigurationStatusException e) {
                            LOGGER.warn("Failed attempting to connect to " + client.getEndpoint()
                                + " during refresh check.");

                            clientFactory.backoffClientClient(originEndpoint, client.getEndpoint());
                        }
                    }
                } else {
                    LOGGER.debug("Skipping configuration refresh check for " + originEndpoint);
                }

                FeatureFlagStore featureStore = connection.getFeatureFlagStore();

                if (featureStore.getEnabled() && StateHolder.getLoadStateFeatureFlag(originEndpoint)) {
                    for (AppConfigurationReplicaClient client : clients) {
                        try {
                            refreshWithTimeFeatureFlags(client, featureStore,
                                StateHolder.getStateFeatureFlag(originEndpoint),
                                monitor.getFeatureFlagRefreshInterval(),
                                eventData, profiles);
                            if (eventData.getDoRefresh()) {
                                clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                return eventData;
                            }
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (AppConfigurationStatusException e) {
                            LOGGER.warn("Failed attempting to connect to " + client.getEndpoint()
                                + " during refresh check.");

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
        AppConfigurationReplicaClientFactory clientFactory, FeatureFlagStore featureStore, List<String> profiles) {
        return refreshStoreCheck(client, clientFactory.findOriginForEndpoint(client.getEndpoint())).block()
            || refreshStoreFeatureFlagCheck(featureStore, client, profiles);
    }

    /**
     * This is for a <b>refresh fail only</b>.
     *
     * @param client Client checking for refresh
     * @param originEndpoint config store origin endpoint
     * @return A refresh should be triggered.
     */
    private static Mono<Boolean> refreshStoreCheck(AppConfigurationReplicaClient client, String originEndpoint) {
        RefreshEventData eventData = new RefreshEventData();
        if (StateHolder.getLoadState(originEndpoint)) {
            return refreshWithoutTime(client, StateHolder.getState(originEndpoint).getWatchKeys(), eventData);
        }
        return Mono.just(false);
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * @param featureStore Feature info for the store
     * @param profiles Current configured profiles, can be used as labels.
     * @param client Client checking for refresh
     * @return true if a refresh should be triggered.
     */
    private static boolean refreshStoreFeatureFlagCheck(FeatureFlagStore featureStore,
        AppConfigurationReplicaClient client, List<String> profiles) {
        RefreshEventData eventData = new RefreshEventData();
        String endpoint = client.getEndpoint();

        if (featureStore.getEnabled() && StateHolder.getLoadStateFeatureFlag(endpoint)) {
            refreshWithoutTimeFeatureFlags(client, featureStore,
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
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @param eventData Info for this refresh event.
     */
    private static void refreshWithTime(AppConfigurationReplicaClient client, State state, Duration refreshInterval,
        RefreshEventData eventData) throws AppConfigurationStatusException {
        if (Instant.now().isAfter(state.getNextRefreshCheck())) {

            refreshWithoutTime(client, state.getWatchKeys(), eventData);

            if (eventData.getDoRefresh()) {
                // Just need to reset refreshInterval, if a refresh was triggered it will be updated after loading the
                // new configurations.
                StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
            }
        }
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param client Client checking for refresh
     * @param watchKeys Watch keys for the store.
     * @param eventData Refresh event info
     */
    private static Mono<Boolean> refreshWithoutTime(AppConfigurationReplicaClient client,
        List<ConfigurationSetting> watchKeys, RefreshEventData eventData) throws AppConfigurationStatusException {
        return Flux.fromIterable(watchKeys).flatMap(watchKey -> {
            return client.getWatchKey(watchKey.getKey(), watchKey.getLabel()).map(configurationSetting -> {
                // If there is no result, etag will be considered empty.
                // A refresh will trigger once the selector returns a value.
                if (configurationSetting != null) {
                    checkETag(watchKey, configurationSetting, client.getEndpoint(), eventData);
                    if (eventData.getDoRefresh()) {
                        return true;
                    }
                }
                return false;
            });
        }).filter(Boolean::booleanValue).next().defaultIfEmpty(true);
    }

    private static void refreshWithTimeFeatureFlags(AppConfigurationReplicaClient client,
        FeatureFlagStore featureStore, State state, Duration refreshInterval, RefreshEventData eventData,
        List<String> profiles) throws AppConfigurationStatusException {
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {

            int watchedKeySize = 0;

            for (FeatureFlagKeyValueSelector watchKey : featureStore.getSelects()) {
                String keyFilter = SELECT_ALL_FEATURE_FLAGS;

                if (StringUtils.hasText(watchKey.getKeyFilter())) {
                    keyFilter = FEATURE_FLAG_PREFIX + watchKey.getKeyFilter();
                }

                SettingSelector selector = new SettingSelector().setKeyFilter(keyFilter)
                    .setLabelFilter(watchKey.getLabelFilterText(profiles));

                PagedFlux<ConfigurationSetting> settings = client.listSettings(selector);
                settings.map(setting -> NormalizeNull.normalizeNullLabel(setting));

                watchedKeySize += checkFeatureFlags(settings.collectList().block(), state, client, eventData);
            }

            if (!eventData.getDoRefresh() && watchedKeySize != state.getWatchKeys().size()) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }

            // Just need to reset refreshInterval, if a refresh was triggered it will be updated after loading the new
            // configurations.
            StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
        }
    }

    private static int checkFeatureFlags(List<ConfigurationSetting> currentKeys, State state,
        AppConfigurationReplicaClient client, RefreshEventData eventData) {
        int watchedKeySize = 0;
        for (ConfigurationSetting currentKey : currentKeys) {
            if (currentKey instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(currentKey.getContentType())) {

                watchedKeySize += 1;
                for (ConfigurationSetting watchFlag : state.getWatchKeys()) {

                    // If there is no result, etag will be considered empty.
                    // A refresh will trigger once the selector returns a value.
                    if (compairKeys(watchFlag, currentKey, client.getEndpoint(), eventData)) {
                        if (eventData.getDoRefresh()) {
                            return watchedKeySize;
                        }
                    }
                }
            }
        }
        return watchedKeySize;
    }

    private static Mono<Boolean> refreshWithoutTimeFeatureFlags(AppConfigurationReplicaClient client,
        FeatureFlagStore featureStore, List<ConfigurationSetting> watchKeys, RefreshEventData eventData,
        List<String> profiles) throws AppConfigurationStatusException {
        for (FeatureFlagKeyValueSelector watchKey : featureStore.getSelects()) {
            String keyFilter = SELECT_ALL_FEATURE_FLAGS;

            if (StringUtils.hasText(watchKey.getKeyFilter())) {
                keyFilter = FEATURE_FLAG_PREFIX + watchKey.getKeyFilter();
            }

            SettingSelector selector = new SettingSelector().setKeyFilter(keyFilter)
                .setLabelFilter(watchKey.getLabelFilterText(profiles));
            PagedFlux<ConfigurationSetting> currentTriggerConfigurations = client.listSettings(selector);

            // TODO (mametcal): This is the issue. First we need to compare the feature flags known to the client and
            // feature flags in the store. Any difference in etag causes a refresh.
            // TODO (mametcal): Then we also need to know if any feature flags were added or removed. A single loop
            // would not work with this.
            Stream<Boolean> changedFeatureFlags = currentTriggerConfigurations.toStream()
                .map(currentTriggerConfiguration -> {
                    watchKeys.stream().map(watchFlag -> {
                        // If there is no result, etag will be considered empty.
                        // A refresh will trigger once the selector returns a value.
                        if (compairKeys(watchFlag, currentTriggerConfiguration, client.getEndpoint(), eventData)) {
                            return eventData.getDoRefresh();
                        }
                        return false;
                    });
                    return false;
                });

            long watchedKeySize = currentTriggerConfigurations.toStream().count();

            if (watchedKeySize != watchKeys.size()) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }

            // TODO (mametcal): At this point we would need to look at the results of both the diff of etags and size and return true if any are different.
            // TODO (mametcal): We need to do this without requesting the results again
            return Flux.fromStream(changedFeatureFlags).filter(Boolean::booleanValue)
                .next()
                .defaultIfEmpty(false);
        }
        return Mono.just(false);
    }

    private static boolean compairKeys(ConfigurationSetting key1, ConfigurationSetting key2,
        String endpoint, RefreshEventData eventData) {
        if (key1 != null && key1.getKey().equals(key2.getKey()) && key1.getLabel().equals(key2.getLabel())) {
            return checkETag(key1, key2, endpoint, eventData);
        }
        return false;

    }

    private static boolean checkETag(ConfigurationSetting watchSetting,
        ConfigurationSetting currentTriggerConfiguration,
        String endpoint, RefreshEventData eventData) {
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
            return true;
        }
        return false;
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
