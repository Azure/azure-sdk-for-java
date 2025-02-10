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
 * Manages all client connections for all configuration stores.
 */
public class AppConfigurationReplicaClientFactory {

    private static final Map<String, ConnectionManager> CONNECTIONS = new HashMap<>();

    private final List<ConfigStore> configStores;

    /**
     * Sets up Connections to all configuration stores.
     *
     * @param clientBuilder builder for app configuration replica clients
     * @param configStores configuration info for config stores
     */
    public AppConfigurationReplicaClientFactory(AppConfigurationReplicaClientsBuilder clientBuilder,
        List<ConfigStore> configStores, ReplicaLookUp replicaLookUp) {
        this.configStores = configStores;
        if (CONNECTIONS.size() == 0) {
            for (ConfigStore store : configStores) {
                ConnectionManager manager = new ConnectionManager(clientBuilder, store, replicaLookUp);
                CONNECTIONS.put(manager.getMainEndpoint(), manager);
            }
        }
    }

    /**
     * @return the connections
     */
    public Map<String, ConnectionManager> getConnections() {
        return CONNECTIONS;
    }

    /**
     * Returns the current used endpoint for a given config store.
     * @param originEndpoint identifier of the store. The identifier is the primary endpoint of the store.
     * @return ConfigurationClient for accessing App Configuration
     */
    List<AppConfigurationReplicaClient> getAvailableClients(String originEndpoint) {
        return CONNECTIONS.get(originEndpoint).getAvailableClients();
    }

    /**
     * Returns the current used endpoint for a given config store.
     * @param originEndpoint identifier of the store. The identifier is the primary endpoint of the store.
     * @return ConfigurationClient for accessing App Configuration
     */
    List<AppConfigurationReplicaClient> getAvailableClients(String originEndpoint, Boolean useCurrent) {
        return CONNECTIONS.get(originEndpoint).getAvailableClients(useCurrent);
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
     * @param endpoint Endpoint to check for replicas
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

    void updateSyncToken(String originEndpoint, String endpoint, String syncToken) {
        CONNECTIONS.get(originEndpoint).updateSyncToken(endpoint, syncToken);
    }

}
