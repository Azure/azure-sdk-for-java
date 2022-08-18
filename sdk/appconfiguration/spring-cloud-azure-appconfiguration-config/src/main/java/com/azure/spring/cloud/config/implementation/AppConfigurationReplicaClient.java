// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Instant;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

/**
 * Client for connecting to App Configuration when multiple replicas are in use.
 */
class AppConfigurationReplicaClient {

    private final String endpoint;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    /**
     * Holds Configuration Client and info needed to manage backoff.
     * @param endpoint client endpoint
     * @param client Configuration Client to App Configuration store
     */
    AppConfigurationReplicaClient(String endpoint, ConfigurationClient client) {
        this.endpoint = endpoint;
        this.client = client;
        this.backoffEndTime = Instant.now().minusMillis(1);
        this.failedAttempts = 0;
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
     * @return The first returned configuration.
     */
    ConfigurationSetting getWatchKey(String key, String label)
        throws HttpResponseException, AppConfigurationStatusException {
        try {
            ConfigurationSetting watchKey = NormalizeNull
                .normalizeNullLabel(client.getConfigurationSetting(key, label));
            this.failedAttempts = 0;
            return watchKey;
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();

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
     * @return List of Configuration Settings.
     */
    PagedIterable<ConfigurationSetting> listConfigurationSettings(SettingSelector settingSelector)
        throws HttpResponseException, AppConfigurationStatusException {
        try {
            PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(settingSelector);
            this.failedAttempts = 0;
            return settings;
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();

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

}
