// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.config.implementation.health.AppConfigurationStoreHealth;
<<<<<<< HEAD
=======
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProviderProperties;
>>>>>>> afebced172d6914fd3335bcf193c158c13e96e70
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.config.implementation.properties.FeatureFlagStore;

/**
 * Holds a set of connections to an app configuration store with zero to many geo-replications.
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final String originEndpoint;

    // Used if multiple connection method is given.
    private List<AppConfigurationReplicaClient> clients;

    private String currentReplica;

    private AppConfigurationStoreHealth health;

    private final AppConfigurationReplicaClientsBuilder clientBuilder;

    private final ConfigStore configStore;

    /**
     * Creates a set of connections to a app configuration store.
     * @param clientBuilder
     * @param configStore Connection info for the store
     * @param clientBuilder Builder for App Configuration Clients
     * @param configStore Connection info for the store
     * @param appProperties Properties for setting up the connection
     */
    ConnectionManager(AppConfigurationReplicaClientsBuilder clientBuilder, ConfigStore configStore) {
        this.clientBuilder = clientBuilder;
        this.configStore = configStore;
        this.originEndpoint = configStore.getEndpoint();
        this.health = AppConfigurationStoreHealth.NOT_LOADED;
        this.currentReplica = configStore.getEndpoint();
    }

    /**
     * Gets the current health information on the Connection to the Config Store
     * @return AppConfigurationConfigStoreHealth
     */
    AppConfigurationStoreHealth getHealth() {
        return this.health;
    }

    void setCurrentClient(String replicaEndpoint) {
        this.currentReplica = replicaEndpoint;
    }

    /**
     * @return the originEndpoint
     */
    String getOriginEndpoint() {
        return originEndpoint;
    }

    /**
     * Returns a client.
     * @return ConfigurationClient
     */
    List<AppConfigurationReplicaClient> getAvailableClients() {
        return getAvailableClients(false);
    }

    /**
     * Returns a client.
     * @return ConfigurationClient
     */
    List<AppConfigurationReplicaClient> getAvailableClients(Boolean useCurrent) {
        if (clients == null) {
            clients = clientBuilder.buildClients(configStore);

            if (clients.size() == 0) {
                this.health = AppConfigurationStoreHealth.NOT_LOADED;
            }
        }

        List<AppConfigurationReplicaClient> availableClients = new ArrayList<>();
        boolean foundCurrent = !useCurrent;

        if (clients.size() == 1) {
            availableClients.add(clients.get(0));
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
            if (availableClients.size() == 0) {
                this.health = AppConfigurationStoreHealth.DOWN;
            } else {
                this.health = AppConfigurationStoreHealth.UP;
            }
        }

        return availableClients;
    }

    List<String> getAllEndpoints() {
        return clients.stream().map(client -> client.getEndpoint()).collect(Collectors.toList());
    }

    /**
     * Call when the current client failed
     * @param endpoint replica endpoint
     */
    void backoffClient(String endpoint) {
        for (AppConfigurationReplicaClient client : clients) {
            if (client.getEndpoint().equals(endpoint)) {
                int failedAttempt = client.getFailedAttempts();
                long backoffTime = BackoffTimeCalculator.calculateBackoff(failedAttempt);
                client.updateBackoffEndTime(Instant.now().plusNanos(backoffTime));
                break;
            }
        }
    }

    /**
     * Updates the sync token of the client. Only works if no replicas are being used.
     *
     * @param syncToken App Configuration sync token
     */
    void updateSyncToken(String syncToken) {
        // Currently sync tokens aren't supported in geo-replication
        if (clients.size() == 1) {
            clients.get(0).updateSyncToken(syncToken);
        }
    }

    AppConfigurationStoreMonitoring getMonitoring() {
        return configStore.getMonitoring();
    }

    FeatureFlagStore getFeatureFlagStore() {
        return configStore.getFeatureFlags();
    }
    
    AppConfigurationStoreMonitoring getMonitoring() {
        return configStore.getMonitoring();
    }

    FeatureFlagStore getFeatureFlagStore() {
        return configStore.getFeatureFlags();
    }
}
