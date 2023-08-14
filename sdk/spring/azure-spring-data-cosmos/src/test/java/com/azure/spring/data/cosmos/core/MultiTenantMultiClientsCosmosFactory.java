// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.spring.data.cosmos.CosmosFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Example for extending CosmosFactory for Mutli-Tenancy at the client level
 */
public class MultiTenantMultiClientsCosmosFactory extends CosmosFactory {

    public static class Client {
        final CosmosAsyncClient cosmosAsyncClient;
        final String databaseName;

        public Client(final CosmosAsyncClient cosmosAsyncClient,final String databaseName) {
            this.cosmosAsyncClient = cosmosAsyncClient;
            this.databaseName = databaseName;
        }
    }

    public static class MultiTenantClients {
        private final Map<String, Client> clientsMap = new HashMap<>();
        public List<String> getAllClientNames() {
            return clientsMap.keySet().stream().sorted().collect(Collectors.toList());
        }

        public Optional<Client> get(final String clientName) {
            return Optional.ofNullable(clientsMap.get(clientName));
        }

        public Optional<Client> getFirstClient() {
            return clientsMap.entrySet().stream().findFirst().map(Map.Entry::getValue);
        }

        public void add(final String clientName, final CosmosAsyncClient client) {
            clientsMap.put(clientName, new Client(client, clientName));
        }
    }

    public final ThreadLocal<String> tenantId = new ThreadLocal<>();
    private final MultiTenantClients clients;

    public MultiTenantMultiClientsCosmosFactory(final MultiTenantClients clients) {
        super(clients.getFirstClient().get().cosmosAsyncClient, clients.getFirstClient().get().databaseName);
        this.clients = clients;
    }

    @Override
    public CosmosAsyncClient getCosmosAsyncClient() {
        return clients.clientsMap.get(tenantId.get()).cosmosAsyncClient;
    }

    @Override
    public String getDatabaseName() {
        return clients.clientsMap.get(tenantId.get()).databaseName;
    }
}
