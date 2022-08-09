// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.config.health.AppConfigurationStoreHealth;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Holds a set of connections to an app configuration store with zero to many geo-replications.
 */
public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final String originEndpoint;

    private final Long defaultMinBackoff;

    private final Long defaultMaxBackoff;

    // Used if only one connection method is given.
    private AppConfigurationReplicaClient client;

    // Used if multiple connection method is given.
    private List<AppConfigurationReplicaClient> clients;

    private String currentReplica;

    private AppConfigurationStoreHealth health;

    private final AppConfigurationReplicaClientsBuilder clientBuilder;

    private final ConfigStore configStore;

    /**
     * Creates a set of connections to an app configuration store.
     * @param configStore Connection info for the store
     * @param appProperties Properties for setting up the connection
     */
    ConnectionManager(AppConfigurationReplicaClientsBuilder clientBuilder, ConfigStore configStore,
        AppConfigurationProviderProperties appProperties) {
        this.clientBuilder = clientBuilder;
        this.defaultMaxBackoff = appProperties.getDefaultMaxBackoff();
        this.defaultMinBackoff = appProperties.getDefaultMinBackoff();
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

            if (clients.size() == 1) {
                client = clients.get(0);
                clients.clear();
            }

            if (client == null && clients.size() == 0) {
                this.health = AppConfigurationStoreHealth.NOT_LOADED;
            }
        }

        List<AppConfigurationReplicaClient> availableClients = new ArrayList<>();
        boolean foundCurrent = !useCurrent;

        if (client != null) {
            availableClients.add(client);
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
        if (client != null) {
            return Arrays.asList(client.getEndpoint());
        }
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
                long backoffTime = BackoffTimeCalculator.calculateBackoff(failedAttempt, defaultMaxBackoff,
                    defaultMinBackoff);
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
        client.updateSyncToken(syncToken);
    }
}
