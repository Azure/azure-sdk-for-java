// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

/**
 * Manages all client connections for all configuration stores with support for replica failover.
 */
public class AppConfigurationReplicaClientFactory {

    /** Map of connection managers keyed by origin endpoint */
    private static final Map<String, ConnectionManager> CONNECTIONS = new HashMap<>();

    /** List of configured stores for endpoint resolution */
    private final List<ConfigStore> configStores;

    /**
     * Sets up connections to all configuration stores with replica support.
     *
     * @param clientBuilder builder for creating app configuration replica clients
     * @param configStores configuration information for all config stores
     * @param replicaLookUp service for discovering and managing replica endpoints
     */
    AppConfigurationReplicaClientFactory(AppConfigurationReplicaClientsBuilder clientBuilder,
        List<ConfigStore> configStores, ReplicaLookUp replicaLookUp) {
        this.configStores = configStores;
        if (CONNECTIONS.isEmpty()) {
            for (ConfigStore store : configStores) {
                ConnectionManager manager = new ConnectionManager(clientBuilder, store, replicaLookUp);
                CONNECTIONS.put(manager.getMainEndpoint(), manager);
            }
        }
    }

    /**
     * Gets all connection managers mapped by their origin endpoints.
     * 
     * @return map of endpoint to connection manager
     */
    public Map<String, ConnectionManager> getConnections() {
        return CONNECTIONS;
    }

    /**
     * Returns available replica clients for a given configuration store.
     * 
     * @param originEndpoint identifier of the store (primary endpoint)
     * @return list of available replica clients for the store
     */
    List<AppConfigurationReplicaClient> getAvailableClients(String originEndpoint) {
        return getAvailableClients(originEndpoint, false);
    }

    /**
     * Returns available replica clients for a given configuration store with current client preference.
     * 
     * @param originEndpoint identifier of the store (primary endpoint)
     * @param useCurrent whether to prefer the currently active client
     * @return list of available replica clients for the store
     */
    List<AppConfigurationReplicaClient> getAvailableClients(String originEndpoint, Boolean useCurrent) {
        return CONNECTIONS.get(originEndpoint).getAvailableClients(useCurrent);
    }

    /**
     * Sets backoff time for a specific replica client due to connection failure.
     * 
     * @param originEndpoint identifier of the store (primary endpoint)
     * @param endpoint the specific replica endpoint that failed
     */
    void backoffClient(String originEndpoint, String endpoint) {
        CONNECTIONS.get(originEndpoint).backoffClient(endpoint);
    }

    /**
     * Gets the health status of all managed configuration store connections.
     * 
     * @return map of origin endpoint to health status
     */
    Map<String, AppConfigurationStoreHealth> getHealth() {
        Map<String, AppConfigurationStoreHealth> health = new HashMap<>();

        CONNECTIONS.forEach((key, value) -> health.put(key, value.getHealth()));

        return health;
    }

    /**
     * Finds the origin endpoint for a given replica endpoint.
     *
     * @param endpoint the replica endpoint to find the origin for
     * @return the origin endpoint, or the input endpoint if no mapping is found
     */
    String findOriginForEndpoint(String endpoint) {
        for (ConfigStore store : configStores) {
            List<String> replicas = store.getEndpoints();
            if (replicas != null && replicas.contains(endpoint)) {
                return store.getEndpoint();
            }
        }
        return endpoint;
    }

    /**
     * Sets the current active replica for a configuration store.
     * 
     * @param originEndpoint the origin configuration store endpoint
     * @param replicaEndpoint the replica endpoint that was successfully connected to
     */
    void setCurrentConfigStoreClient(String originEndpoint, String replicaEndpoint) {
        CONNECTIONS.get(originEndpoint).setCurrentClient(replicaEndpoint);
    }

    /**
     * Updates the sync token for a specific replica endpoint.
     * 
     * @param originEndpoint the origin configuration store endpoint
     * @param endpoint the specific replica endpoint
     * @param syncToken the new sync token to store
     */
    void updateSyncToken(String originEndpoint, String endpoint, String syncToken) {
        CONNECTIONS.get(originEndpoint).updateSyncToken(endpoint, syncToken);
    }

}
