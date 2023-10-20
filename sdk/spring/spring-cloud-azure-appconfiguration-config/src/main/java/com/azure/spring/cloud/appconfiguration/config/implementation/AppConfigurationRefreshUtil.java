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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store
     * has a value that needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    static Mono<RefreshEventData> refreshStoresCheck(AppConfigurationReplicaClientFactory clientFactory,
        Duration refreshInterval, List<String> profiles, Long defaultMinBackoff) {
        Mono<RefreshEventData> eventData;
        BaseAppConfigurationPolicy.setWatchRequests(true);

        try {
            if (refreshInterval != null && StateHolder.getNextForcedRefresh() != null
                && Instant.now().isAfter(StateHolder.getNextForcedRefresh())) {
                String eventDataInfo = "Minimum refresh period reached. Refreshing configurations.";

                LOGGER.info(eventDataInfo);

                return Mono.just(new RefreshEventData().setFullMessage(eventDataInfo));
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
                            eventData = refreshWithTime(client, StateHolder.getState(originEndpoint),
                                monitor.getRefreshInterval());

                            eventData.map(refreshEvent -> {
                                if (refreshEvent.getDoRefresh()) {
                                    clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                    return refreshEvent;
                                }
                                return refreshEvent;
                            });
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (AppConfigurationStatusException e) {
                            LOGGER.warn(
                                "Failed attempting to connect to " + client.getEndpoint() + " during refresh check.");

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
                            eventData = Mono.just(refreshWithTimeFeatureFlags(client, featureStore,
                                StateHolder.getStateFeatureFlag(originEndpoint),
                                monitor.getFeatureFlagRefreshInterval(), profiles));

                            eventData.map(refreshEvent -> {
                                if (refreshEvent.getDoRefresh()) {
                                    clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                    return refreshEvent;
                                }
                                return refreshEvent;
                            });
                            // (TODO) mametcal: If check didn't throw an error other clients don't need to be checked.
                        } catch (AppConfigurationStatusException e) {
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
        return Mono.just(new RefreshEventData());
    }

    static Mono<RefreshEventData> checkStoreAfterRefreshFailed(AppConfigurationReplicaClient client,
        AppConfigurationReplicaClientFactory clientFactory, FeatureFlagStore featureStore, List<String> profiles) {
        return refreshStoreCheck(client, clientFactory.findOriginForEndpoint(client.getEndpoint()))
            .mergeWith(refreshStoreFeatureFlagCheck(featureStore, client, profiles)).reduce((a, b) -> a.merge(b));
    }

    /**
     * This is for a <b>refresh fail only</b>.
     *
     * @param client Client checking for refresh
     * @param originEndpoint config store origin endpoint
     * @return A refresh should be triggered.
     */
    private static Flux<RefreshEventData> refreshStoreCheck(AppConfigurationReplicaClient client,
        String originEndpoint) {
        if (StateHolder.getLoadState(originEndpoint)) {
            return refreshWithoutTime(client, StateHolder.getState(originEndpoint).getWatchKeys());
        }
        return Flux.just(new RefreshEventData());
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * 
     * @param featureStore Feature info for the store
     * @param profiles Current configured profiles, can be used as labels.
     * @param client Client checking for refresh
     * @return true if a refresh should be triggered.
     */
    private static Flux<RefreshEventData> refreshStoreFeatureFlagCheck(FeatureFlagStore featureStore,
        AppConfigurationReplicaClient client, List<String> profiles) {
        String endpoint = client.getEndpoint();
        if (featureStore.getEnabled() && StateHolder.getLoadStateFeatureFlag(endpoint)) {
            return refreshWithoutTimeFeatureFlags(client, featureStore,
                StateHolder.getStateFeatureFlag(endpoint).getWatchKeys(), profiles);
        } else {
            LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
        }
        return Flux.just(new RefreshEventData());
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param state The refresh state of the endpoint being checked.
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @param eventData Info for this refresh event.
     * @return
     */
    private static Mono<RefreshEventData> refreshWithTime(AppConfigurationReplicaClient client, State state,
        Duration refreshInterval) throws AppConfigurationStatusException {
        if (Instant.now().isAfter(state.getNextRefreshCheck())) {

            return refreshWithoutTime(client, state.getWatchKeys()).reduce((a, b) -> a.merge(b)).map(result -> {
                if (result.getDoRefresh()) {
                    // Just need to reset refreshInterval, if a refresh was triggered it will be updated after loading
                    // the new configurations.
                    StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
                }
                return result;
            });
        }
        return Mono.just(new RefreshEventData());
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param client Client checking for refresh
     * @param watchKeys Watch keys for the store.
     * @param eventData Refresh event info
     */
    private static Flux<RefreshEventData> refreshWithoutTime(AppConfigurationReplicaClient client,
        List<ConfigurationSetting> watchKeys) throws AppConfigurationStatusException {
        return Flux.fromStream(watchKeys.stream()).flatMap(watchKey -> {
            return client.getWatchKey(watchKey.getKey(), watchKey.getLabel()).map(watchedKey -> {
                // If there is no result, etag will be considered empty.
                // A refresh will trigger once the selector returns a value.
                if (watchedKey != null) {
                    return checkETag(watchKey, watchedKey, client.getEndpoint());
                }
                return new RefreshEventData();
            });
        });
    }

    private static RefreshEventData refreshWithTimeFeatureFlags(AppConfigurationReplicaClient client,
        FeatureFlagStore featureStore, State state, Duration refreshInterval, List<String> profiles)
        throws AppConfigurationStatusException {
        Instant date = Instant.now();
        RefreshEventData eventData = new RefreshEventData();
        if (date.isAfter(state.getNextRefreshCheck())) {

            int watchedKeySize = 0;

            for (FeatureFlagKeyValueSelector watchKey : featureStore.getSelects()) {
                String keyFilter = SELECT_ALL_FEATURE_FLAGS;

                if (StringUtils.hasText(watchKey.getKeyFilter())) {
                    keyFilter = FEATURE_FLAG_PREFIX + watchKey.getKeyFilter();
                }

                SettingSelector selector =
                    new SettingSelector().setKeyFilter(keyFilter).setLabelFilter(watchKey.getLabelFilterText(profiles));
                List<ConfigurationSetting> currentKeys = client.listSettings(selector).collectList().block();

                if (currentKeys == null) {
                    return eventData;
                }

                eventData = checkFeatureFlags(currentKeys, state, client);

                if (eventData.getDoRefresh()) {
                    watchedKeySize += 1;
                }
            }

            if (!eventData.getDoRefresh() && watchedKeySize != state.getWatchKeys().size()) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }

            // Just need to reset refreshInterval, if a refresh was triggered it will be updated after loading
            // the new configurations.
            StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
        }
        return eventData;
    }

    private static RefreshEventData checkFeatureFlags(List<ConfigurationSetting> currentKeys, State state,
        AppConfigurationReplicaClient client) {
        for (ConfigurationSetting currentKey : currentKeys) {
            if (currentKey instanceof FeatureFlagConfigurationSetting
                && FEATURE_FLAG_CONTENT_TYPE.equals(currentKey.getContentType())) {
                for (ConfigurationSetting watchFlag : state.getWatchKeys()) {

                    // If there is no result, etag will be considered empty.
                    // A refresh will trigger once the selector returns a value.
                    RefreshEventData eventData = compairKeys(watchFlag, currentKey, client.getEndpoint());
                    if (eventData.getDoRefresh()) {
                        return eventData;
                    }
                }
            }
        }
        return new RefreshEventData();
    }


    private static Flux<RefreshEventData> refreshWithoutTimeFeatureFlags(AppConfigurationReplicaClient client,
        FeatureFlagStore featureStore, List<ConfigurationSetting> watchKeys, List<String> profiles)
        throws AppConfigurationStatusException {
        Flux<RefreshEventData> results = Flux.empty();
        for (FeatureFlagKeyValueSelector watchKey : featureStore.getSelects()) {
            String keyFilter = SELECT_ALL_FEATURE_FLAGS;

            if (StringUtils.hasText(watchKey.getKeyFilter())) {
                keyFilter = FEATURE_FLAG_PREFIX + watchKey.getKeyFilter();
            }

            SettingSelector selector =
                new SettingSelector().setKeyFilter(keyFilter).setLabelFilter(watchKey.getLabelFilterText(profiles));
            List<ConfigurationSetting> currentTriggerConfigurations =
                client.listSettings(selector).collectList().block();

            if (currentTriggerConfigurations == null) {
                continue;
            }

            int watchedKeySize = 0;

            for (ConfigurationSetting currentTriggerConfiguration : currentTriggerConfigurations) {
                watchedKeySize += 1;

                results.mergeWith(Flux.fromStream(watchKeys.stream())
                    .map(watchFlag -> compairKeys(watchFlag, currentTriggerConfiguration, client.getEndpoint())));
            }

            int finalWatchKeySize = watchedKeySize;

            results.reduce((a, b) -> a.merge(b)).map(result -> {
                if (finalWatchKeySize != watchKeys.size()) {
                    String eventDataInfo = ".appconfig.featureflag/*";

                    // Only one refresh Event needs to be call to update all of the
                    // stores, not one for each.
                    LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                    return result.setMessage(eventDataInfo);
                }
                return result;
            });
        }
        return results;
    }

    private static RefreshEventData compairKeys(ConfigurationSetting key1, ConfigurationSetting key2, String endpoint) {
        if (key1 != null && key1.getKey().equals(key2.getKey()) && key1.getLabel().equals(key2.getLabel())) {
            return checkETag(key1, key2, endpoint);
        }
        return new RefreshEventData();

    }

    private static RefreshEventData checkETag(ConfigurationSetting watchSetting,
        ConfigurationSetting currentTriggerConfiguration, String endpoint) {
        RefreshEventData eventData = new RefreshEventData();
        if (currentTriggerConfiguration == null) {
            return eventData;
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
        return eventData;
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

        public RefreshEventData merge(RefreshEventData b) {
            this.doRefresh = this.doRefresh || b.doRefresh;
            if (StringUtils.hasText(this.message) && StringUtils.hasText(b.getMessage())) {
                this.message += "; " + b.getMessage();
            } else {
                this.message += b.getMessage();
            }
            return this;
        }
    }
}
