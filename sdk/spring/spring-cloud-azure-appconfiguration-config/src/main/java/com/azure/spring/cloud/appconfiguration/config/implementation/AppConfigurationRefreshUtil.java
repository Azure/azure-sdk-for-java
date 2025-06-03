// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.PUSH_REFRESH;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlagState;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring.PushNotification;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

/**
 * Utility class for handling Azure App Configuration refresh operations.
 */
public class AppConfigurationRefreshUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationRefreshUtil.class);

    /**
     * Checks all configured stores to determine if any configurations need to be refreshed.
     *
     * @param clientFactory factory for accessing App Configuration clients
     * @param refreshInterval the time interval between refresh checks
     * @param defaultMinBackoff the minimum backoff time for failed operations
     * @param replicaLookUp component for handling replica failover
     * @return RefreshEventData containing information about whether a refresh should occur
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

                boolean pushRefresh = false;
                PushNotification notification = monitor.getPushNotification();
                if ((notification.getPrimaryToken() != null
                    && StringUtils.hasText(notification.getPrimaryToken().getName()))
                    || (notification.getSecondaryToken() != null
                        && StringUtils.hasText(notification.getPrimaryToken().getName()))) {
                    pushRefresh = true;
                }
                Context context = new Context("refresh", true).addData(PUSH_REFRESH, pushRefresh);

                List<AppConfigurationReplicaClient> clients = clientFactory.getAvailableClients(originEndpoint);

                if (monitor.isEnabled() && StateHolder.getLoadState(originEndpoint)) {
                    for (AppConfigurationReplicaClient client : clients) {
                        try {
                            refreshWithTime(client, StateHolder.getState(originEndpoint), monitor.getRefreshInterval(),
                                eventData, replicaLookUp, context);
                            if (eventData.getDoRefresh()) {
                                clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                return eventData;
                            }
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (HttpResponseException e) {
                            LOGGER.warn(
                                "Failed to connect to App Configuration store {} during configuration refresh check. "
                                    + "Status: {}, Message: {}",
                                client.getEndpoint(), e.getResponse().getStatusCode(), e.getMessage());

                            clientFactory.backoffClient(originEndpoint, client.getEndpoint());
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
                                monitor.getFeatureFlagRefreshInterval(), eventData, replicaLookUp, context);
                            if (eventData.getDoRefresh()) {
                                clientFactory.setCurrentConfigStoreClient(originEndpoint, client.getEndpoint());
                                return eventData;
                            }
                            // If check didn't throw an error other clients don't need to be checked.
                            break;
                        } catch (HttpResponseException e) {
                            LOGGER.warn(
                                "Failed to connect to App Configuration store {} during feature flag refresh check. "
                                    + "Status: {}, Message: {}",
                                client.getEndpoint(), e.getResponse().getStatusCode(), e.getMessage());

                            clientFactory.backoffClient(originEndpoint, client.getEndpoint());
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

    /**
     * Performs a refresh check for a specific store client without time constraints. This method is used for refresh
     * failure scenarios only.
     *
     * @param client the client for checking refresh status
     * @param originEndpoint the original config store endpoint
     * @param context the operation context
     * @return true if a refresh should be triggered, false otherwise
     */
    static boolean refreshStoreCheck(AppConfigurationReplicaClient client, String originEndpoint, Context context) {
        RefreshEventData eventData = new RefreshEventData();
        if (StateHolder.getLoadState(originEndpoint)) {
            refreshWithoutTime(client, StateHolder.getState(originEndpoint).getWatchKeys(), eventData, context);
        }
        return eventData.getDoRefresh();
    }

    /**
     * Performs a feature flag refresh check for a specific store client. This method is used for refresh failure
     * scenarios only.
     * 
     * @param featureStoreEnabled whether feature store is enabled
     * @param client the client for checking refresh status
     * @param context the operation context
     * @return true if a refresh should be triggered, false otherwise
     */
    static boolean refreshStoreFeatureFlagCheck(Boolean featureStoreEnabled,
        AppConfigurationReplicaClient client, Context context) {
        RefreshEventData eventData = new RefreshEventData();
        String endpoint = client.getEndpoint();

        if (featureStoreEnabled && StateHolder.getStateFeatureFlag(endpoint) != null) {
            refreshWithoutTimeFeatureFlags(client, StateHolder.getStateFeatureFlag(endpoint), eventData, context);
        } else {
            LOGGER.debug("Skipping feature flag refresh check for " + endpoint);
        }
        return eventData.getDoRefresh();
    }

    /**
     * Checks configuration refresh triggers for etag changes with time-based validation. Only performs the refresh
     * check if the refresh interval has elapsed.
     *
     * @param client the App Configuration client to use for checking
     * @param state the current refresh state of the endpoint being checked
     * @param refreshInterval the time duration to wait until next check of this endpoint
     * @param eventData the refresh event data to update if changes are detected
     * @param replicaLookUp component for updating auto-failover endpoints
     * @param context the operation context
     * @throws AppConfigurationStatusException if there's an error during the refresh check
     */
    private static void refreshWithTime(AppConfigurationReplicaClient client, State state, Duration refreshInterval,
        RefreshEventData eventData, ReplicaLookUp replicaLookUp, Context context)
        throws AppConfigurationStatusException {
        if (Instant.now().isAfter(state.getNextRefreshCheck())) {
            replicaLookUp.updateAutoFailoverEndpoints();
            refreshWithoutTime(client, state.getWatchKeys(), eventData, context);

            StateHolder.getCurrentState().updateStateRefresh(state, refreshInterval);
        }
    }

    /**
     * Checks configuration refresh triggers for etag changes without time validation. This method immediately checks
     * all watch keys for changes regardless of refresh intervals.
     *
     * @param client the App Configuration client to use for checking
     * @param watchKeys the list of configuration settings to watch for changes
     * @param eventData the refresh event data to update if changes are detected
     * @param context the operation context
     * @throws AppConfigurationStatusException if there's an error during the refresh check
     */
    private static void refreshWithoutTime(AppConfigurationReplicaClient client, List<ConfigurationSetting> watchKeys,
        RefreshEventData eventData, Context context) throws AppConfigurationStatusException {
        for (ConfigurationSetting watchKey : watchKeys) {
            ConfigurationSetting watchedKey = client.getWatchKey(watchKey.getKey(), watchKey.getLabel(), context);

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

    /**
     * Checks feature flag refresh triggers with time-based validation. Only performs the refresh check if the refresh
     * interval has elapsed.
     *
     * @param client the App Configuration client to use for checking
     * @param state the current feature flag state of the endpoint being checked
     * @param refreshInterval the time duration to wait until next check of this endpoint
     * @param eventData the refresh event data to update if changes are detected
     * @param replicaLookUp component for updating auto-failover endpoints
     * @param context the operation context
     * @throws AppConfigurationStatusException if there's an error during the refresh check
     */
    private static void refreshWithTimeFeatureFlags(AppConfigurationReplicaClient client, FeatureFlagState state,
        Duration refreshInterval, RefreshEventData eventData, ReplicaLookUp replicaLookUp, Context context)
        throws AppConfigurationStatusException {
        Instant date = Instant.now();
        if (date.isAfter(state.getNextRefreshCheck())) {
            replicaLookUp.updateAutoFailoverEndpoints();

            for (FeatureFlags featureFlags : state.getWatchKeys()) {
                if (client.checkWatchKeys(featureFlags.getSettingSelector(), context)) {
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

    /**
     * Checks feature flag refresh triggers without time validation. This method immediately checks all feature flag
     * watch keys for changes regardless of refresh intervals.
     *
     * @param client the App Configuration client to use for checking
     * @param watchKeys the feature flag state containing watch keys to check for changes
     * @param eventData the refresh event data to update if changes are detected
     * @param context the operation context
     * @throws AppConfigurationStatusException if there's an error during the refresh check
     */
    private static void refreshWithoutTimeFeatureFlags(AppConfigurationReplicaClient client, FeatureFlagState watchKeys,
        RefreshEventData eventData, Context context) throws AppConfigurationStatusException {

        for (FeatureFlags featureFlags : watchKeys.getWatchKeys()) {
            if (client.checkWatchKeys(featureFlags.getSettingSelector(), context)) {
                String eventDataInfo = ".appconfig.featureflag/*";

                // Only one refresh Event needs to be call to update all of the
                // stores, not one for each.
                LOGGER.info("Configuration Refresh Event triggered by " + eventDataInfo);

                eventData.setMessage(eventDataInfo);
            }

        }
    }

    /**
     * Checks the etag values between watched and current configuration settings to determine if a refresh is needed.
     * 
     * @param watchSetting the configuration setting being watched for changes
     * @param currentTriggerConfiguration the current configuration setting from the store
     * @param endpoint the endpoint of the configuration store
     * @param eventData the refresh event data to update if a change is detected
     */
    private static void checkETag(ConfigurationSetting watchSetting, ConfigurationSetting currentTriggerConfiguration,
        String endpoint, RefreshEventData eventData) {
        if (currentTriggerConfiguration == null) {
            return;
        }

        LOGGER.debug("Comparing eTags - watched: {} vs current: {}",
            watchSetting.getETag(), currentTriggerConfiguration.getETag());

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
     * Data structure containing information about a refresh event.
     */
    static class RefreshEventData {

        private static final String MSG_TEMPLATE = "Some keys matching %s has been updated since last check.";

        private String message;

        private boolean doRefresh = false;

        /**
         * Creates a new RefreshEventData with empty message and refresh flag set to false.
         */
        RefreshEventData() {
            this.message = "";
        }

        /**
         * Sets the refresh message using the standard message template.
         * 
         * @param prefix the prefix to include in the message (typically a key name)
         * @return this RefreshEventData instance for method chaining
         */
        RefreshEventData setMessage(String prefix) {
            setFullMessage(String.format(MSG_TEMPLATE, prefix));
            return this;
        }

        /**
         * Sets the full refresh message and marks that a refresh should occur.
         * 
         * @param message the complete message describing the refresh event
         * @return this RefreshEventData instance for method chaining
         */
        private RefreshEventData setFullMessage(String message) {
            this.message = message;
            this.doRefresh = true;
            return this;
        }

        /**
         * Gets the refresh event message.
         * 
         * @return the message describing what triggered the refresh
         */
        public String getMessage() {
            return this.message;
        }

        /**
         * Indicates whether a refresh should be performed.
         * 
         * @return true if a refresh is needed, false otherwise
         */
        public boolean getDoRefresh() {
            return doRefresh;
        }
    }
}
