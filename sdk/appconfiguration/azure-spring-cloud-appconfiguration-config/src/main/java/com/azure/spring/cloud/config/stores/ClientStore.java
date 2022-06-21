// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.ClientManager;
import com.azure.spring.cloud.config.NormalizeNull;

/**
 * Client for connecting to and getting keys from an Azure App Configuration Instance
 */
public final class ClientStore {

    private final ClientManager clientManager;

    /**
     * Creates Client store for connecting to App Configuration
     * @param clientManager Manages connections to each config store
     */
    public ClientStore(ClientManager clientManager) {
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
        return NormalizeNull.normalizeNullLabel(clientManager.getClient(endpoint).getConfigurationSetting(key, label));
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
        return clientManager.getClient(storeName).listConfigurationSettings(settingSelector);
    }

    /**
     * Gets a list of Configuration Settings from the given config store that match the Setting Selector criteria.
     *
     * @param settingSelector Information on which setting to pull. i.e. number of results, key value...
     * @param storeName Name of the App Configuration store to query against.
     * @return List of Configuration Settings.
     */
    public PagedIterable<ConfigurationSetting> listSettings(SettingSelector settingSelector, String storeName) {
        return clientManager.getClient(storeName).listConfigurationSettings(settingSelector);
    }

    /**
     * Update the sync token for a client store.
     * @param storeName the name of the client store.
     * @param syncToken the sync token.
     */
    public void updateSyncToken(String storeName, String syncToken) {
        if (syncToken != null) {
            clientManager.getClient(storeName).updateSyncToken(syncToken);
        }
    }
}
