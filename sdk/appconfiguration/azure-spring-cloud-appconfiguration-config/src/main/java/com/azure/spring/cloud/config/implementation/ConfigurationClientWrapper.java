// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Instant;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.NormalizeNull;
import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;

/**
 * Wrapper for Configuration Client to manage backoff.
 */
class ConfigurationClientWrapper {

    private final String endpoint;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    private AppConfigurationStoreHealth health;

    /**
     * Wrapper for Configuration Client to manage backoff.
     * @param endpoint client endpoint
     * @param client Configuration Client to App Configuration store
     */
    ConfigurationClientWrapper(String endpoint, ConfigurationClient client) {
        this.endpoint = endpoint;
        this.client = client;
        this.backoffEndTime = Instant.now();
        this.failedAttempts = 0;
        this.health = AppConfigurationStoreHealth.UP;
    }

    /**
     * @return backOffEndTime
     */
    Instant getBackoffEndTime() {
        return backoffEndTime;
    }

    /**
     * Updates the backoff time and increases the number of failed attempts.
     * @param backoffEndTime next time this client can be used.
     */
    void updateBackoffEndTime(Instant backoffEndTime) {
        this.backoffEndTime = backoffEndTime;
        this.failedAttempts += 1;
    }

    /**
     * @return number of failed attempts
     */
    int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Sets the number of failed attempts to 0.
     */
    void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    /**
     * @return endpoint
     */
    String getEndpoint() {
        return endpoint;
    }

    /**
     * @return ConfiguraitonClinet
     */
    ConfigurationClient getClient() {
        return client;
    }

    /**
     * Gets the Configuration Setting for the given config store that match the Setting Selector criteria. Follows
     * retry-after-ms header.
     *
     * @param key String value of the watch key
     * @param label String value of the watch key, use \0 for null.
     * @param endpoint Endpoint of the App Configuration store to query against.
     * @return The first returned configuration.
     */
    ConfigurationSetting getWatchKey(String key, String label)
        throws HttpResponseException, AppConfigurationStatusException {
        try {
            ConfigurationSetting watchKey = NormalizeNull
                .normalizeNullLabel(client.getConfigurationSetting(key, label));
            this.health = AppConfigurationStoreHealth.UP;
            return watchKey;
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();

            this.health = AppConfigurationStoreHealth.DOWN;
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                throw new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
            }
            throw e;
        }
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     */
    PagedIterable<ConfigurationSetting> listSettings(SettingSelector settingSelector)
        throws HttpResponseException, AppConfigurationStatusException {
        try {
            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(settingSelector);
            this.health = AppConfigurationStoreHealth.UP;
            return settings;
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();

            this.health = AppConfigurationStoreHealth.DOWN;
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                throw new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
            }
            throw e;
        }
    }

    /**
     * Update the sync token for a client store.
     * @param syncToken the sync token.
     */
    void updateSyncToken(String syncToken) {
        if (syncToken != null) {
            client.updateSyncToken(syncToken);
        }
    }

    AppConfigurationStoreHealth getHealth() {
        return health;
    }

    void setHealth(AppConfigurationStoreHealth health) {
        this.health = health;
    }

}
