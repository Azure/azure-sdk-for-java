// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Manages all client connections for all configuration stores.
 */
public class AppConfigurationReplicaClientFactory {

    private static final Map<String, ConnectionManager> CONNECTIONS = new HashMap<>();

    private final List<ConfigStore> configStores;

    /**
     * Sets up Connections to all configuration stores.
     * 
     * @param properties client properties
     * @param appProperties library properties  
     */
    public AppConfigurationReplicaClientFactory(AppConfigurationReplicaClientBuilder clientBuilder,
        AppConfigurationProperties properties, AppConfigurationProviderProperties appProperties) {
        this.configStores = properties.getStores();
        if (CONNECTIONS.size() == 0) {
            for (ConfigStore store : properties.getStores()) {
                ConnectionManager manager = new ConnectionManager(clientBuilder, store, appProperties);
                CONNECTIONS.put(manager.getOriginEndpoint(), manager);
            }
        }
    }

    /**
     * Returns the current used endpoint for a given config store.
     * @param originEndpoint identifier of the store. The identifier is the primary endpoint of the store.
     * @return ConfigurationClient for accessing App Configuration
     */
    List<AppConfigurationReplicaClient> getAvailableClients(String originEndpoint) {
        return CONNECTIONS.get(originEndpoint).getAvalibleClients();
    }

    /**
     * Sets backoff time for the current client that is being used, and attempts to get a new one.
     * @param originEndpoint identifier of the store. The identifier is the primary endpoint of the store.
     * @param endpoint replica endpoint
     */
    void backoffClientClient(String originEndpoint, String endpoint) {
        CONNECTIONS.get(originEndpoint).backoffClient(endpoint);
    }

    /**
     * Gets the health of the client connections to App Configuration
     * @return map of endpoint origin it's health
     */
    Map<String, AppConfigurationStoreHealth> getHealth() {
        Map<String, AppConfigurationStoreHealth> health = new HashMap<>();

        CONNECTIONS.forEach((key, value) -> health.put(key, value.getHealth()));

        return health;
    }

    /**
     * Returns the origin endpoint for a given endpoint. If not found will return the given endpoint;
     * 
     * @param endpoint App Configuration Endpoint
     * @return String Endpoint
     */
    String findOriginForEndpoint(String endpoint) {
        for (ConfigStore store : configStores) {
            for (String replica : store.getEndpoints()) {
                if (replica.equals(endpoint)) {
                    return store.getEndpoint();
                }
            }
        }
        return endpoint;
    }

    /**
     * Checks if a given endpoint has any configured replicas.
     * @param endpoint Endpoint to check for replics
     * @return true if at least one other unique endpoint connects to the same configuration store
     */
    boolean hasReplicas(String endpoint) {
        String originEndpoint = findOriginForEndpoint(endpoint);
        for (ConfigStore store : configStores) {
            if (store.getEndpoint().equals(originEndpoint)) {
                if (store.getConnectionStrings().size() > 0 || store.getEndpoints().size() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the replica as the currently used endpoint for connecting to the config store.
     * @param originEndpoint Origin Configuration Store
     * @param replicaEndpoint Replica that was last successfully connected to.
     */
    void setCurrentConfigStoreClient(String originEndpoint, String replicaEndpoint) {
        CONNECTIONS.get(originEndpoint).setCurrentClient(replicaEndpoint);
    }

    /**
     * Gets the current replica to connect to when refreshing configurations.
     * @param originEndpoint Origin replica
     * @return endpoint
     */
    String getCurrentConfigStoreClient(String originEndpoint) {
        return CONNECTIONS.get(originEndpoint).getCurrentClient();
    }

}
