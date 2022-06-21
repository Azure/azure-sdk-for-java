// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.HashMap;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Manages all client connections for all configuration stores.
 */
public class ClientManager {

    private final HashMap<String, ConnectionManager> connections;

    ClientManager(AppConfigurationProperties properties, AppConfigurationProviderProperties appProperties,
        AppConfigurationCredentialProvider tokenCredentialProvider,
        ConfigurationClientBuilderSetup clientProvider, Boolean isDev, Boolean isKeyVaultConfigured) {
        this.connections = new HashMap<>();
        for (ConfigStore store : properties.getStores()) {
            String clientId = "";
            
            if (properties.getManagedIdentity() != null) {
                clientId = properties.getManagedIdentity().getClientId();
            }
            
            ConnectionManager manager = new ConnectionManager(store, appProperties, tokenCredentialProvider,
                clientProvider, isDev, isKeyVaultConfigured, clientId);
            connections.put(manager.getStoreIdentifier(), manager);
        }
    }
    
    /**
     * Returns the current used endpoint for a given config store.
     * @param endpoint StoreIdentifier the endpoint for the first store listed in the config.
     * @return ConfigurationClient for accessing App Configuration
     */
    public ConfigurationClient getClient(String endpoint) {
        return connections.get(endpoint).getClient();
    }

}
