// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import reactor.core.publisher.Mono;

import java.util.*;

public class ReadmeSamples {
    private final String serviceEndpoint = "<service-endpoint>";
    private final String key = "<key>";
    private final DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
    private final GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();

    private final CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildAsyncClient();

    private final CosmosClient cosmosClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildClient();

    private final CosmosAsyncContainer cosmosAsyncContainer = cosmosAsyncClient
        .getDatabase("<YOUR DATABASE NAME>")
        .getContainer("<YOUR CONTAINER NAME>");

    private final CosmosContainer cosmosContainer = cosmosClient
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

    public void readItemAsync() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readItem
        // Read an item
        cosmosAsyncContainer.readItem(passenger.getId(), new PartitionKey(passenger.getId()), Passenger.class)
            .flatMap(response -> Mono.just(response.getItem()))
            .subscribe(passengerItem -> System.out.println(passengerItem), throwable -> {
                CosmosException cosmosException = (CosmosException) throwable;
                cosmosException.printStackTrace();
            });
        // ...
        // END: com.azure.cosmos.CosmosAsyncContainer.readItem
    }

    public void readItem() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosContainer.readItem
        // Read an item
        try {
            CosmosItemResponse<Passenger> response = cosmosContainer.readItem(
                passenger.getId(),
                new PartitionKey(passenger.getId()),
                Passenger.class
            );
            Passenger passengerItem = response.getItem();
        } catch (NotFoundException e) {
            // catch exception if item not found
            System.out.printf("Passenger with item id %s not found\n",
                passenger.getId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // ...
        // END: com.azure.cosmos.CosmosContainer.readItem
    }

    public void deleteItemAsyncSample() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.deleteItem

        cosmosAsyncContainer.deleteItem(
            passenger.getId(),
            new PartitionKey(passenger.getId())
        ).subscribe(response -> {
            System.out.println(response);
        }, throwable -> {
            CosmosException cosmosException = (CosmosException) throwable;
            cosmosException.printStackTrace();
        });
        // END: com.azure.cosmos.CosmosAsyncContainer.deleteItem
    }

    public void deleteItemSample() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosContainer.deleteItem
        try {
            CosmosItemRequestOptions options = new CosmosItemRequestOptions();
            CosmosItemResponse<Object> deleteItemResponse = cosmosContainer.deleteItem(
                passenger.getId(),
                new PartitionKey(passenger.getId()),
                options
            );
            System.out.println(deleteItemResponse);
        } catch (NotFoundException e) {
            // catch exception if item not found
            System.out.printf("Passenger with item id %s not found\n",
                passenger.getId());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // END: com.azure.cosmos.CosmosContainer.deleteItem
    }

    public void patchItemSample() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosContainer.patchItem
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();

        cosmosPatchOperations
            .add("/departure", "SEA")
            .increment("/trips", 1);

        CosmosItemResponse<Passenger> response = cosmosContainer.patchItem(
            passenger.getId(),
            new PartitionKey(passenger.getId()),
            cosmosPatchOperations,
            Passenger.class);
        // END: com.azure.cosmos.CosmosContainer.patchItem
    }

    public void replaceItemSample() {
        Passenger oldPassenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        Passenger newPassenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosContainer.replaceItem
        CosmosItemResponse<Passenger> response = cosmosContainer.replaceItem(
            newPassenger,
            oldPassenger.getId(),
            new PartitionKey(oldPassenger.getId()),
            new CosmosItemRequestOptions());
        // END: com.azure.cosmos.CosmosContainer.replaceItem
    }

    public void readAllItemsSample() {
        String partitionKey = "partitionKey";
        // BEGIN: com.azure.cosmos.CosmosContainer.readAllItems
        CosmosPagedIterable<Passenger> passengers = cosmosContainer
            .readAllItems(new PartitionKey(partitionKey), Passenger.class);

        passengers.forEach(passenger -> {
            System.out.println(passenger);
        });
        // END: com.azure.cosmos.CosmosContainer.readAllItems
    }

    public void readManySample() {
        String passenger1Id = "item1";
        String passenger2Id = "item1";

        // BEGIN: com.azure.cosmos.CosmosContainer.readMany
        List<CosmosItemIdentity> itemIdentityList = new ArrayList<>();
        itemIdentityList.add(new CosmosItemIdentity(new PartitionKey(passenger1Id), passenger1Id));
        itemIdentityList.add(new CosmosItemIdentity(new PartitionKey(passenger2Id), passenger2Id));

        FeedResponse<Passenger> passengerFeedResponse = cosmosContainer.readMany(itemIdentityList, Passenger.class);
        for (Passenger passenger : passengerFeedResponse.getResults()) {
            System.out.println(passenger);
        }
        // END: com.azure.cosmos.CosmosContainer.readMany
    }

    public void queryChangeFeedSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.queryChangeFeed
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
            .createForProcessingFromNow(FeedRange.forFullRange())
            .allVersionsAndDeletes();

        Iterable<FeedResponse<Passenger>> feedResponses = cosmosContainer.queryChangeFeed(options, Passenger.class)
            .iterableByPage();
        for (FeedResponse<Passenger> feedResponse : feedResponses) {
            List<Passenger> results = feedResponse.getResults();
            System.out.println(results);
        }
        // END: com.azure.cosmos.CosmosContainer.queryChangeFeed
    }

    public void queryItemsSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.queryItems
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        String query = "SELECT * FROM Passenger WHERE Passenger.departure IN ('SEA', 'IND')";
        Iterable<FeedResponse<Passenger>> queryResponses = cosmosContainer.queryItems(query, options, Passenger.class)
            .iterableByPage();

        for (FeedResponse<Passenger> feedResponse : queryResponses) {
            List<Passenger> results = feedResponse.getResults();
            System.out.println(results);
        }
        // END: com.azure.cosmos.CosmosContainer.queryItems
    }

    public void queryItemsQuerySpecSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.SqlQuerySpec.queryItems
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        String query = "SELECT * FROM Passenger p WHERE (p.departure = @departure)";
        List<SqlParameter> parameters = Collections.singletonList(new SqlParameter("@departure", "SEA"));
        SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query, parameters);

        Iterable<FeedResponse<Passenger>> queryResponses = cosmosContainer.queryItems(sqlQuerySpec, options, Passenger.class)
            .iterableByPage();

        for (FeedResponse<Passenger> feedResponse : queryResponses) {
            List<Passenger> results = feedResponse.getResults();
            System.out.println(results);
        }
        // END: com.azure.cosmos.CosmosContainer.SqlQuerySpec.queryItems
    }

    static final class Passenger {
        private final String id;
        private final String email;
        private final String name;
        private int trips;
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
