package com.azure.spring.cloud.config.implementation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.ClientFactory;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.pipline.policies.BaseAppConfigurationPolicy;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;

public class AppConfigurationRefreshUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPullRefresh.class);

    private static ConcurrentHashMap<String, AppConfigurationStoreHealth> clientHealth = new ConcurrentHashMap<String, AppConfigurationStoreHealth>();

    /**
     * Goes through each config store and checks if any of its keys need to be refreshed. If any store has a value that
     * needs to be updated a refresh event is called after every store is checked.
     *
     * @return If a refresh event is called.
     */
    static RefreshEventData refreshStoresCheck(AppConfigurationProviderProperties appProperties, ClientFactory clientFactory, List<ConfigStore> configStores,
        Duration refreshInterval) {
        BaseAppConfigurationPolicy.setWatchRequests(true);
        RefreshEventData eventData = new RefreshEventData();

        // TODO (mametcal) This just looks incorrect. Either that or document of why it isn't wrong.
        configStores.forEach(store -> {
            if (getStoreHealthState(store)) {
                clientHealth.put(store.getEndpoint(), AppConfigurationStoreHealth.DOWN);
            } else {
                clientHealth.put(store.getEndpoint(), AppConfigurationStoreHealth.NOT_LOADED);
            }
        });
        try {
            if (refreshInterval != null && StateHolder.getNextForcedRefresh() != null
                && Instant.now().isAfter(StateHolder.getNextForcedRefresh())) {
                String eventDataInfo = "Minimum refresh period reached. Refreshing configurations.";

                LOGGER.info(eventDataInfo);

                return new RefreshEventData(eventDataInfo);
            }

            for (ConfigStore configStore : configStores) {
                if (configStore.isEnabled()) {
                    String endpoint = configStore.getEndpoint();
                    AppConfigurationStoreMonitoring monitor = configStore.getMonitoring();
                    for (ConfigurationClientWrapper client : clientFactory.getAvailableClients(endpoint)) {
                        if (StateHolder.getLoadState(endpoint)) {
                            if (monitor.isEnabled()) {
                                eventData = refreshWithTime(client, StateHolder.getState(endpoint),
                                    endpoint,
                                    monitor.getRefreshInterval());
                                if (eventData.getDoRefresh()) {
                                    break;
                                }
                            } else {
                                LOGGER.debug("Skipping configuration refresh check for " + endpoint);
                            }
                            clientHealth.put(configStore.getEndpoint(), AppConfigurationStoreHealth.UP);
                        }

                        FeatureFlagStore featureStore = configStore.getFeatureFlags();

                        if (StateHolder.getLoadStateFeatureFlag(endpoint)) {
                            if (featureStore.getEnabled()) {
                                eventData = refreshWithTimeFeatureFlags(client, configStore.getFeatureFlags(),
                                    StateHolder.getStateFeatureFlag(endpoint), monitor.getFeatureFlagRefreshInterval());
                                if (eventData.getDoRefresh()) {
                                    break;
                                }
                            } else {
                                LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
                            }
                            clientHealth.put(configStore.getEndpoint(), AppConfigurationStoreHealth.UP);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // The next refresh will happen sooner if refresh interval is expired.
            StateHolder.updateNextRefreshTime(refreshInterval, appProperties);
            throw e;
        } 

        return eventData;
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * @param configStores
     * @param clientFactory
     * @return
     */
    public static RefreshEventData refreshStoreCheck(ConfigurationClientWrapper client,
        ClientFactory clientFactory) {
        RefreshEventData configCheck = new RefreshEventData();
        String endpoint = client.getEndpoint();
        try {

            if (StateHolder.getLoadState(endpoint)) {
                configCheck = refreshWithoutTime(client, StateHolder.getState(endpoint).getWatchKeys());
                clientHealth.put(endpoint, AppConfigurationStoreHealth.UP);
            }
        } catch (Exception e) {
            clientHealth.put(endpoint, AppConfigurationStoreHealth.DOWN);
            throw e;
        }
        return configCheck;
    }

    /**
     * This is for a <b>refresh fail only</b>.
     * @param configStores
     * @param clientFactory
     * @return
     */
    public static boolean refreshStoreFeatureFlagCheck(ConfigStore configStore, ConfigurationClientWrapper client,
        ClientFactory clientFactory) {
        RefreshEventData eventData = new RefreshEventData();

        try {
            String endpoint = configStore.getEndpoint();

            if (StateHolder.getLoadStateFeatureFlag(endpoint)) {

                if (configStore.getFeatureFlags().getEnabled()) {
                    eventData = refreshWithoutTimeFeatureFlags(client, configStore,
                        StateHolder.getStateFeatureFlag(endpoint).getWatchKeys());

                } else {
                    LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
                }
                // TODO (mametcal) This doesn't seem right
                clientHealth.put(configStore.getEndpoint(), AppConfigurationStoreHealth.UP);
            }

        } catch (Exception e) { // this might break AppConfigurationStatusException usage 
            clientHealth.put(configStore.getEndpoint(), AppConfigurationStoreHealth.DOWN);
            throw e;
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
    private static RefreshEventData refreshWithTime(ConfigurationClientWrapper client, State state, String endpoint,
        Duration refreshInterval) throws AppConfigurationStatusException {
        RefreshEventData eventData = new RefreshEventData();

        if (Instant.now().isAfter(state.getNextRefreshCheck())) {

            eventData = refreshWithoutTime(client, state.getWatchKeys());

            if (eventData.getDoRefresh()) {
                // Just need to reset refreshInterval, if a refresh was triggered it will updated after loading the new
                // configurations.
                StateHolder.updateStateRefresh(state, refreshInterval);
            }
        }

        return eventData;
    }

    /**
     * Checks refresh trigger for etag changes. If they have changed a RefreshEventData is published.
     *
     * @param state The refresh state of the endpoint being checked.
     * @param endpoint The App Config Endpoint being checked for refresh.
     * @param refreshInterval Amount of time to wait until next check of this endpoint.
     * @return Refresh event was triggered. No other sources need to be checked.
     */
    private static RefreshEventData refreshWithoutTime(ConfigurationClientWrapper client,
        List<ConfigurationSetting> watchKeys) throws AppConfigurationStatusException {
        for (ConfigurationSetting watchKey : watchKeys) {
            ConfigurationSetting watchedKey = client.getWatchKey(watchKey.getKey(), watchKey.getLabel());

            String etag = null;
            // If there is no result, etag will be considered empty.
            // A refresh will trigger once the selector returns a value.
            if (watchedKey != null) {
                etag = watchedKey.getETag();
            }

            RefreshEventData check = checkETag(watchKey, watchedKey, etag, client.getEndpoint());
            if (check.getDoRefresh()) {
                break;
            }
        }

        return new RefreshEventData();
    }

    private static RefreshEventData refreshWithTimeFeatureFlags(ConfigurationClientWrapper client,
        FeatureFlagStore featureStore, State state, Duration refreshInterval) throws AppConfigurationStatusException {
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {
            SettingSelector selector = new SettingSelector().setKeyFilter(featureStore.getKeyFilter())
                .setLabelFilter(featureStore.getLabelFilter());
            PagedIterable<ConfigurationSetting> currentKeys = client.listSettings(selector);

            int watchedKeySize = 0;


            keyCheck: // TODO (mametcal Need to rethink this
            for (ConfigurationSetting currentKey : currentKeys) {
                    
                watchedKeySize += 1;
                for (ConfigurationSetting watchFlag : state.getWatchKeys()) {

                    String etag = null;
                    // If there is no result, etag will be considered empty.
                    // A refresh will trigger once the selector returns a value.
                    if (watchFlag != null) {
                        etag = watchFlag.getETag();
                    } else {
                        break keyCheck;
                    }

                    if (watchFlag.getKey().equals(currentKey.getKey())) {
                        RefreshEventData check = checkETag(watchFlag, currentKey, etag, client.getEndpoint());
                        if (check.getDoRefresh()) {
                            break keyCheck;
                        }
                    }
                }
            }

            if (watchedKeySize != state.getWatchKeys().size()) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                return new RefreshEventData(eventDataInfo);
            }

            // Just need to reset refreshInterval, if a refresh was triggered it will updated after loading the new
            // configurations.
            StateHolder.updateStateRefresh(state, refreshInterval);
        }

        return new RefreshEventData();
    }

    private static RefreshEventData refreshWithoutTimeFeatureFlags(ConfigurationClientWrapper client,
        ConfigStore configStore, List<ConfigurationSetting> watchKeys) throws AppConfigurationStatusException {
        SettingSelector selector = new SettingSelector().setKeyFilter(configStore.getFeatureFlags().getKeyFilter())
            .setLabelFilter(configStore.getFeatureFlags().getLabelFilter());
        PagedIterable<ConfigurationSetting> currentKeys = client.listSettings(selector);

        int watchedKeySize = 0;

        for (ConfigurationSetting currentKey : currentKeys) {
            watchedKeySize += 1;
            for (ConfigurationSetting watchFlag : watchKeys) {

                String etag = null;
                // If there is no result, etag will be considered empty.
                // A refresh will trigger once the selector returns a value.
                if (watchFlag != null) {
                    etag = watchFlag.getETag();
                } else {
                    break;
                }

                if (watchFlag.getKey().equals(currentKey.getKey())) {
                    RefreshEventData check = checkETag(watchFlag, currentKey, etag, client.getEndpoint());
                    if (check.getDoRefresh()) {
                        return check;
                    }
                }
            }
        }

        if (watchedKeySize != watchKeys.size()) {
            String eventDataInfo = ".appconfig.featureflag/*";

            // Only one refresh Event needs to be call to update all of the
            // stores, not one for each.
            LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

            return new RefreshEventData(eventDataInfo);
        }

        return new RefreshEventData();
    }

    private static RefreshEventData checkETag(ConfigurationSetting watchSetting, ConfigurationSetting currentWatch,
        String etag, String endpoint) {
        LOGGER.debug(etag + " - " + currentWatch.getETag());
        if (etag != null && !etag.equals(currentWatch.getETag())) {
            LOGGER.trace(
                "Some keys in store [{}] matching the key [{}] and label [{}] is updated, "
                    + "will send refresh event.",
                endpoint, watchSetting.getKey(), watchSetting.getLabel());

            String eventDataInfo = watchSetting.getKey();

            // Only one refresh Event needs to be call to update all of the
            // stores, not one for each.
            LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

            return new RefreshEventData(eventDataInfo);
        }
        return new RefreshEventData();
    }

    private static Boolean getStoreHealthState(ConfigStore store) {
        return store.isEnabled() && (StateHolder.getLoadState(store.getEndpoint())
            || StateHolder.getLoadStateFeatureFlag(store.getEndpoint()));
    }

    /**
     * For each refresh, multiple etags can change, but even one etag is changed, refresh is required.
     */
    static class RefreshEventData {

        private static final String MSG_TEMPLATE = "Some keys matching %s has been updated since last check.";

        private final String message;

        private boolean doRefresh = false;

        public RefreshEventData() {
            this.message = "";
        }

        RefreshEventData(String prefix) {
            this.message = String.format(MSG_TEMPLATE, prefix);
            this.doRefresh = true;
        }

        public String getMessage() {
            return this.message;
        }

        public boolean getDoRefresh() {
            return doRefresh;
        }
    }
    
    static Map<String, AppConfigurationStoreHealth> getAppConfigurationStoresHealth() {
        return clientHealth;
    }
}
