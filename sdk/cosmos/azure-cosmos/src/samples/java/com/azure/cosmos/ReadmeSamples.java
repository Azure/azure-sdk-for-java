// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;

import java.util.Arrays;
import java.util.UUID;

public class ReadmeSamples {
    private final String serviceEndpoint = "<service-endpoint>";
    private final String key = "<key>";
    private final DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
    private final GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();

    private final CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildAsyncClient();
    private final CosmosAsyncContainer cosmosAsyncContainer = cosmosAsyncClient
        .getDatabase("<YOUR DATABASE NAME>")
        .getContainer("<YOUR CONTAINER NAME>");

    public void createCosmosAsyncClient() {
        // BEGIN: readme-sample-createCosmosAsyncClient
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint(serviceEndpoint)
            .key(key)
            .buildAsyncClient();
        // END: readme-sample-createCosmosAsyncClient
    }

    public void createCosmosClient() {
        // BEGIN: readme-sample-createCosmosClient
        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint(serviceEndpoint)
            .key(key)
            .buildClient();
        // END: readme-sample-createCosmosClient
    }

    public void createCosmosClient2() {
        // BEGIN: readme-sample-createCosmosClient2
        // Create a new CosmosAsyncClient via the CosmosClientBuilder
        // It only requires endpoint and key, but other useful settings are available
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .buildAsyncClient();

        // Create a new CosmosClient via the CosmosClientBuilder
        CosmosClient cosmosClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .buildClient();

        // Create a new CosmosClient with customizations
        cosmosClient = new CosmosClientBuilder()
            .endpoint(serviceEndpoint)
            .key(key)
            .directMode(directConnectionConfig, gatewayConnectionConfig)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .connectionSharingAcrossClientsEnabled(true)
            .contentResponseOnWriteEnabled(true)
            .userAgentSuffix("my-application1-client")
            .preferredRegions(Arrays.asList("West US", "East US"))
            .buildClient();
        // END: readme-sample-createCosmosClient2
    }

    public void createDatabase() {
        // BEGIN: readme-sample-createDatabase
        // Get a reference to the container
        // This will create (or read) a database and its container.
        cosmosAsyncClient.createDatabaseIfNotExists("<YOUR DATABASE NAME>")
            // TIP: Our APIs are Reactor Core based, so try to chain your calls
            .map(databaseResponse -> cosmosAsyncClient.getDatabase(databaseResponse.getProperties().getId()))
            .subscribe(database -> System.out.printf("Created database '%s'.%n", database.getId()));
        // END: readme-sample-createDatabase
    }

    public void createContainer() {
        // BEGIN: readme-sample-createContainer
        cosmosAsyncClient.createDatabaseIfNotExists("<YOUR DATABASE NAME>")
            // TIP: Our APIs are Reactor Core based, so try to chain your calls
            .flatMap(databaseResponse -> {
                String databaseId = databaseResponse.getProperties().getId();
                return cosmosAsyncClient.getDatabase(databaseId)
                    // Create Container
                    .createContainerIfNotExists("<YOUR CONTAINER NAME>", "/id")
                    .map(containerResponse -> cosmosAsyncClient.getDatabase(databaseId)
                        .getContainer(containerResponse.getProperties().getId()));
            })
            .subscribe(container -> System.out.printf("Created container '%s' in database '%s'.%n",
                container.getId(), container.getDatabase().getId()));
        // END: readme-sample-createContainer
    }

    public void crudOperationOnItems() {
        // BEGIN: readme-sample-crudOperationOnItems
        // Create an item
        cosmosAsyncContainer.createItem(new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND"))
            .flatMap(response -> {
                System.out.println("Created item: " + response.getItem());
                // Read that item 👓
                return cosmosAsyncContainer.readItem(response.getItem().getId(),
                    new PartitionKey(response.getItem().getId()), Passenger.class);
            })
            .flatMap(response -> {
                System.out.println("Read item: " + response.getItem());
                // Replace that item 🔁
                Passenger p = response.getItem();
                p.setDestination("SFO");
                return cosmosAsyncContainer.replaceItem(p, response.getItem().getId(),
                    new PartitionKey(response.getItem().getId()));
            })
            // delete that item 💣
            .flatMap(response -> cosmosAsyncContainer.deleteItem(response.getItem().getId(),
                new PartitionKey(response.getItem().getId())))
            .block(); // Blocking for demo purposes (avoid doing this in production unless you must)
        // ...
        // END: readme-sample-crudOperationOnItems
    }

    private static final class Passenger {
        private final String id;
        private final String email;
        private final String name;

        private String departure;
        private String destination;

        Passenger(String email, String name, String departure, String destination) {
            this.id = UUID.randomUUID().toString();
            this.email = email;
            this.name = name;
            this.departure = departure;
            this.destination = destination;
        }

        String getId() {
            return id;
        }

        void setDestination(String destination) {
            this.destination = destination;
        }
    }
}
