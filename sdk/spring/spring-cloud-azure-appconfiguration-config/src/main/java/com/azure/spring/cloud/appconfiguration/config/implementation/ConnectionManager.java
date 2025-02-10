// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;

/**
 * Holds a set of connections to an app configuration store with zero to many geo-replications.
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final String originEndpoint;

    // Used if multiple connection method is given.
    private List<AppConfigurationReplicaClient> clients;

    private Map<String, AppConfigurationReplicaClient> autoFailoverClients;

    private String currentReplica;

    private AppConfigurationStoreHealth health;

    private final AppConfigurationReplicaClientsBuilder clientBuilder;

    private final ConfigStore configStore;

    private final ReplicaLookUp replicaLookUp;

    /**
     * Creates a set of connections to an app configuration store.
     * @param clientBuilder Builder for App Configuration Clients
     * @param configStore Connection info for the store
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
    String getMainEndpoint() {
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

    List<String> getAllEndpoints() {
        List<String> endpoints = clients.stream().map(AppConfigurationReplicaClient::getEndpoint)
            .collect(Collectors.toList());
        endpoints.addAll(replicaLookUp.getAutoFailoverEndpoints(configStore.getEndpoint()));
        return endpoints;
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
                return;
            }
        }

        int failedAttempt = autoFailoverClients.get(endpoint).getFailedAttempts();
        long backoffTime = BackoffTimeCalculator.calculateBackoff(failedAttempt);
        autoFailoverClients.get(endpoint).updateBackoffEndTime(Instant.now().plusNanos(backoffTime));
    }

    /**
     * Updates the sync token of the client. Only works if no replicas are being used.
     *
     * @param syncToken App Configuration sync token
     */
    void updateSyncToken(String endpoint, String syncToken) {
        clients.stream().filter(client -> client.getEndpoint().equals(endpoint)).findFirst()
            .ifPresent(client -> client.updateSyncToken(syncToken));
        autoFailoverClients.values().stream().filter(client -> client.getEndpoint().equals(endpoint)).findFirst()
            .ifPresent(client -> client.updateSyncToken(syncToken));
    }

    AppConfigurationStoreMonitoring getMonitoring() {
        return configStore.getMonitoring();
    }

    FeatureFlagStore getFeatureFlagStore() {
        return configStore.getFeatureFlags();
    }

}
