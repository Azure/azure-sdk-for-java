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

/**
 * Wrapper for Configuration Client to manage backoff.
 */
public class ConfigurationClientWrapper {

    private final String endpoint;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    /**
     * Wrapper for Configuration Client to manage backoff.
     * @param endpoint client endpoint
     * @param client Configuration Client to App Configuration store
     */
    public ConfigurationClientWrapper(String endpoint, ConfigurationClient client) {
        this.endpoint = endpoint;
        this.client = client;
        this.backoffEndTime = Instant.now();
        this.failedAttempts = 0;
    }

    /**
     * @return backOffEndTime
     */
    public Instant getBackoffEndTime() {
        return backoffEndTime;
    }

    /**
     * Updates the backoff time and increases the number of failed attempts.
     * @param backoffEndTime next time this client can be used.
     */
    public void updateBackoffEndTime(Instant backoffEndTime) {
        this.backoffEndTime = backoffEndTime;
        this.failedAttempts += 1;
    }

    /**
     * @return number of failed attempts
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Sets the number of failed attempts to 0.
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    /**
     * @return endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return ConfiguraitonClinet
     */
    public ConfigurationClient getClient() {
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
    public ConfigurationSetting getWatchKey(String key, String label) throws AppConfigurationStatusException {
        try {
            return NormalizeNull
                .normalizeNullLabel(client.getConfigurationSetting(key, label));
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                throw new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
            }
            // TODO (mametcal) ...
            return null;
        }
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     */
    public PagedIterable<ConfigurationSetting> listSettings(SettingSelector settingSelector)
        throws AppConfigurationStatusException {
        try {
            return client.listConfigurationSettings(settingSelector);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                throw new AppConfigurationStatusException(e.getMessage(), e.getResponse(), e.getValue());
            }
            // TODO (mametcal) ...
            return null;
        }
    }

    /**
     * Update the sync token for a client store.
     * @param syncToken the sync token.
     */
    public void updateSyncToken(String syncToken) {
        if (syncToken != null) {
            client.updateSyncToken(syncToken);
        }
    }

}
