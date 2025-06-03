// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

/**
 * Manages connection pools and client lifecycle for Azure App Configuration stores with support for geo-replication,
 * auto-failover, and intelligent client routing.
 */
class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    /** The primary endpoint URL for the App Configuration store. */
    private final String originEndpoint;

    /** List of configured replica clients for the primary App Configuration store. */
    private List<AppConfigurationReplicaClient> clients;

    /** Map of auto-discovered failover clients, keyed by endpoint URL. */
    private final Map<String, AppConfigurationReplicaClient> autoFailoverClients;

    /** Currently active replica endpoint being used for requests. */
    private String currentReplica;

    /** Current health status of the App Configuration store connection. */
    private AppConfigurationStoreHealth health;

    /** Builder for creating App Configuration replica clients. */
    private final AppConfigurationReplicaClientsBuilder clientBuilder;

    /** Configuration store settings and connection parameters. */
    private final ConfigStore configStore;

    /** Service for discovering auto-failover replica endpoints. */
    private final ReplicaLookUp replicaLookUp;

    /**
     * Creates a connection manager for the specified App Configuration store.
     * 
     * @param clientBuilder the builder for creating App Configuration replica clients; must not be null
     * @param configStore the configuration store settings and connection parameters; must not be null
     * @param replicaLookUp the service for discovering auto-failover endpoints; must not be null
     */
    ConnectionManager(AppConfigurationReplicaClientsBuilder clientBuilder, ConfigStore configStore,
        ReplicaLookUp replicaLookUp) {
        this.clientBuilder = clientBuilder;
        this.configStore = configStore;
        this.originEndpoint = configStore.getEndpoint();
        this.health = AppConfigurationStoreHealth.NOT_LOADED;
        this.currentReplica = configStore.getEndpoint();
        this.autoFailoverClients = new HashMap<>();
        this.replicaLookUp = replicaLookUp;
    }

    /**
     * Retrieves the current health status of the App Configuration store connection.
     * 
     * @return the current health status; never null
     */
    AppConfigurationStoreHealth getHealth() {
        return this.health;
    }

    /**
     * Sets the current active replica endpoint for client routing.
     * 
     * @param replicaEndpoint the endpoint URL to set as current; may be null to reset to primary endpoint
     */
    void setCurrentClient(String replicaEndpoint) {
        this.currentReplica = replicaEndpoint;
    }

    /**
     * Retrieves the primary (origin) endpoint URL for the App Configuration store.
     * 
     * @return the primary endpoint URL; never null
     */
    String getMainEndpoint() {
        return originEndpoint;
    }

    /**
     * Retrieves all available App Configuration clients that are ready for use.
     * 
     * @return a list of available clients; may be empty if all clients are currently unavailable
     */
    List<AppConfigurationReplicaClient> getAvailableClients() {
        return getAvailableClients(false);
    }

    /**
     * Retrieves available App Configuration clients with optional current replica preference.
     * 
     * @param useCurrent if true, prioritizes returning clients starting from the current replica; if false, returns all
     * available clients
     * @return a list of available clients ordered by preference; may be empty if all clients are currently unavailable
     */
    List<AppConfigurationReplicaClient> getAvailableClients(Boolean useCurrent) {
        if (clients == null) {
            clients = clientBuilder.buildClients(configStore);

            if (clients.isEmpty()) {
                this.health = AppConfigurationStoreHealth.NOT_LOADED;
            }
        }

        List<AppConfigurationReplicaClient> availableClients = new ArrayList<>();
        boolean foundCurrent = !useCurrent;

        if (clients.size() == 1) {
            if (clients.get(0).getBackoffEndTime().isBefore(Instant.now())) {
                availableClients.add(clients.get(0));
            }
        } else if (clients.size() > 0) {
            for (AppConfigurationReplicaClient replicaClient : clients) {
                if (replicaClient.getEndpoint().equals(currentReplica)) {
                    foundCurrent = true;
                }
                if (foundCurrent && replicaClient.getBackoffEndTime().isBefore(Instant.now())) {
                    LOGGER.debug("Using Client: " + replicaClient.getEndpoint());
                    availableClients.add(replicaClient);
                }
            }
        }

        if (availableClients.size() == 0) {
            List<String> autoFailoverEndpoints = replicaLookUp.getAutoFailoverEndpoints(configStore.getEndpoint());

            if (autoFailoverEndpoints.size() > 0) {
                for (String failoverEndpoint : autoFailoverEndpoints) {
                    AppConfigurationReplicaClient client = autoFailoverClients.get(failoverEndpoint);
                    if (client == null) {
                        client = clientBuilder.buildClient(failoverEndpoint, configStore);
                        autoFailoverClients.put(failoverEndpoint, client);
                    }
                    if (client.getBackoffEndTime().isBefore(Instant.now())) {
                        availableClients.add(client);
                        break;
                    }
                }
            }
        }
        if (clients.size() > 0 && availableClients.size() == 0) {
            this.health = AppConfigurationStoreHealth.DOWN;
        } else if (clients.size() > 0) {
            this.health = AppConfigurationStoreHealth.UP;
        }

        return availableClients;
    }

    /**
     * Applies exponential backoff to a failed client endpoint.
     * 
     * @param endpoint the endpoint URL of the failed client; must not be null or empty
     */
    void backoffClient(String endpoint) {
        for (AppConfigurationReplicaClient client : clients) {
            if (client.getEndpoint().equals(endpoint)) {
                int failedAttempt = client.getFailedAttempts();
                long backoffTime = BackoffTimeCalculator.calculateBackoff(failedAttempt);
                client.updateBackoffEndTime(Instant.now().plusNanos(backoffTime));
                return;
            }
        }

        int failedAttempt = autoFailoverClients.get(endpoint).getFailedAttempts();
        long backoffTime = BackoffTimeCalculator.calculateBackoff(failedAttempt);
        autoFailoverClients.get(endpoint).updateBackoffEndTime(Instant.now().plusNanos(backoffTime));
    }

    /**
     * Updates the synchronization token for the specified client endpoint.
     * 
     * @param endpoint the endpoint URL of the client to update; may be null (method will have no effect if null)
     * @param syncToken the new synchronization token; may be null to clear the existing token
     */
    void updateSyncToken(String endpoint, String syncToken) {
        clients.stream().filter(client -> client.getEndpoint().equals(endpoint)).findFirst()
            .ifPresent(client -> client.updateSyncToken(syncToken));
        autoFailoverClients.values().stream().filter(client -> client.getEndpoint().equals(endpoint)).findFirst()
            .ifPresent(client -> client.updateSyncToken(syncToken));
    }

    /**
     * Retrieves the monitoring configuration for the App Configuration store.
     * 
     * @return the monitoring configuration; may be null if not configured
     */
    AppConfigurationStoreMonitoring getMonitoring() {
        return configStore.getMonitoring();
    }

    /**
     * Retrieves the feature flag store configuration.
     * 
     * @return the feature flag store configuration; may be null if not configured
     */
    FeatureFlagStore getFeatureFlagStore() {
        return configStore.getFeatureFlags();
    }

}
