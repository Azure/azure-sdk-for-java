// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.ClientFactory;
import com.azure.spring.cloud.config.NormalizeNull;
import com.azure.spring.cloud.config.resource.ConfigurationClientWrapper;

/**
 * Client for connecting to and getting keys from an Azure App Configuration Instance
 */
public final class ClientStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientStore.class);

    private final ClientFactory clientManager;

    /**
     * Creates Client store for connecting to App Configuration
     * @param clientManager Manages connections to each config store
     */
    public ClientStore(ClientFactory clientManager) {
        this.clientManager = clientManager;
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
    public ConfigurationSetting getWatchKey(String key, String label, String endpoint) {
        ConfigurationClientWrapper client = clientManager.getClient(endpoint);
        try {
            return NormalizeNull
                .normalizeNullLabel(client.getClient().getConfigurationSetting(key, label));
        } catch (HttpResponseException e) {
            return null;
        }
    }

    /**
     * Used to load all feature flags to track changes for reload.
     * 
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     */
    public PagedIterable<ConfigurationSetting> getFeatureFlagWatchKey(SettingSelector settingSelector,
        String storeName) {
        ConfigurationClientWrapper client = clientManager.getClient(storeName);
        try {
            return client.getClient().listConfigurationSettings(settingSelector);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                return dealWithError(storeName, settingSelector, client, e);
            }
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
    public PagedIterable<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName) {
        ConfigurationClientWrapper client = clientManager.getClient(storeName);
        try {
            return client.getClient().listConfigurationSettings(settingSelector);
        } catch (HttpResponseException e) {
            int statusCode = e.getResponse().getStatusCode();
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                return dealWithError(storeName, settingSelector, client, e);
            }
            return null;
        }
    }

    /**
     * Update the sync token for a client store.
     * @param storeName the name of the client store.
     * @param syncToken the sync token.
     */
    public void updateSyncToken(String storeName, String syncToken) {
        if (syncToken != null) {
            ConfigurationClientWrapper client = clientManager.getClient(storeName);
            client.getClient().updateSyncToken(syncToken);
        }
    }

    private PagedIterable<ConfigurationSetting> dealWithError(String storeName, SettingSelector settingSelector,
        ConfigurationClientWrapper client, HttpResponseException e) {

        ConfigurationClientWrapper newClient = clientManager.resetAndGetNewClient(storeName);

        if (newClient == null) {
            LOGGER.debug("No valid client found.");
            return null;
        }

        try {
            return client.getClient().listConfigurationSettings(settingSelector);
        } catch (HttpResponseException e2) {
            int statusCode = e.getResponse().getStatusCode();
            if (statusCode == 429 || statusCode == 408 || statusCode >= 500) {
                return dealWithError(storeName, settingSelector, newClient, e2);
            }
            return null;
        }
    }
}
