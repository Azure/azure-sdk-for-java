// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.HashMap;
import java.util.List;

import com.azure.spring.cloud.config.implementation.ConfigurationClientWrapper;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Manages all client connections for all configuration stores.
 */
public class ClientFactory {

    private static final HashMap<String, ConnectionManager> CONNECTIONS = new HashMap<>();;

    ClientFactory(AppConfigurationProperties properties, AppConfigurationProviderProperties appProperties,
        AppConfigurationCredentialProvider tokenCredentialProvider,
        ConfigurationClientBuilderSetup clientProvider, Boolean isDev, Boolean isKeyVaultConfigured) {
        if (CONNECTIONS.size() == 0) {
            for (ConfigStore store : properties.getStores()) {
                String clientId = "";

                if (properties.getManagedIdentity() != null) {
                    clientId = properties.getManagedIdentity().getClientId();
                }

                ConnectionManager manager = new ConnectionManager(store, appProperties, tokenCredentialProvider,
                    clientProvider, isDev, isKeyVaultConfigured, clientId);
                CONNECTIONS.put(manager.getStoreIdentifier(), manager);
            }
        }
    }

    /**
     * Returns the current used endpoint for a given config store.
     * @param storeIdentifier identifier of the store. The identifier is the primary endpoint of the store. 
     * @return ConfigurationClient for accessing App Configuration
     */
    public List<ConfigurationClientWrapper> getAvailableClients(String storeIdentifier) {
        return CONNECTIONS.get(storeIdentifier).getAvalibleClients();
    }
    
    /**
     * Sets backoff time for the current client that is being used, and attempts to get a new one.
     * @param storeIdentifier identifier of the store. The identifier is the primary endpoint of the store. 
     * @param endpoint replica endpoint
     */
    public void backoffClientClient(String storeIdentifier, String endpoint) {
        CONNECTIONS.get(storeIdentifier).backoffClient(endpoint);
    }
    
    // TODO (mametcal) need a way to mark a replica as the current one in use.
    
    // TODO (mametcal) need a way to reset in use replicas after refresh finishes in any form.

}
