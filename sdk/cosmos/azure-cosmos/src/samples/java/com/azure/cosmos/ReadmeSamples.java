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

    private final CosmosDatabase cosmosDatabase = cosmosClient
        .getDatabase("<YOUR DATABASE NAME>");
    private final CosmosContainer cosmosContainer = cosmosDatabase
        .getContainer("<YOUR CONTAINER NAME>");
    private final CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure = new CosmosAsyncStoredProcedure("id", cosmosAsyncContainer);
    private final CosmosAsyncTrigger cosmosAsyncTrigger = new CosmosAsyncTrigger("id", cosmosAsyncContainer);

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

    public void databaseReadSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.read
        CosmosDatabase cosmosDatabase = cosmosClient
            .getDatabase("<YOUR DATABASE NAME>");
        CosmosDatabaseResponse readResponse = cosmosDatabase.read();
        // END: com.azure.cosmos.CosmosDatabase.read
    }

    public void databaseDeleteSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.delete
        CosmosDatabase cosmosDatabase = cosmosClient
            .getDatabase("<YOUR DATABASE NAME>");
        CosmosDatabaseResponse deleteResponse = cosmosDatabase.delete();
        // END: com.azure.cosmos.CosmosDatabase.delete
    }

    public void databaseCreateContainerSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainer
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);
        try {
            CosmosContainerResponse container = cosmosDatabase.createContainer(containerProperties);
        } catch (CosmosException ce) {
            System.out.println("Failed to create container: " + ce);
        }
        // END: com.azure.cosmos.CosmosDatabase.createContainer

    }

    public void databaseCreateContainerPropsSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        int autoScaleMaxThroughput = 1000;

        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerProps
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(autoScaleMaxThroughput);
        try {
            CosmosContainerResponse container = cosmosDatabase.createContainer(
                containerProperties,
                throughputProperties
            );
        } catch (CosmosException ce) {
            System.out.println("Failed to create container: " + ce);
        }
        // END: com.azure.cosmos.CosmosDatabase.createContainerProps
    }

    public void databaseCreateContainerThroughputSample() {
        String containerId = "passengers";
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        int throughput = 1000;
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerThroughput
        CosmosContainerProperties containerProperties =
            new CosmosContainerProperties(containerId, partitionKeyDefinition);

        try {
            CosmosContainerResponse container = cosmosDatabase.createContainer(
                containerProperties,
                throughput,
                options
            );
        } catch (CosmosException ce) {
            System.out.println("Failed to create container: " + ce);
        }
        // END: com.azure.cosmos.CosmosDatabase.createContainerThroughput
    }

    public void databaseCreateContainerPartitionKeySample() {
        String containerId = "passengers";
        String partitionKeyPath = "/id";
        int autoscaledThroughput = 1000;

        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerPartitionKey
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(autoscaledThroughput);
        try {
            CosmosContainerResponse container = cosmosDatabase.createContainer(
                containerId,
                partitionKeyPath,
                throughputProperties
            );
        } catch (CosmosException ce) {
            System.out.println("Failed to create container: " + ce);
        }
        // END: com.azure.cosmos.CosmosDatabase.createContainerPartitionKey
    }

    public void cosmosStoredProcedureReadSample1() {
        String id = "ID";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.read_no_params
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);

        CosmosStoredProcedureResponse response = procedure.read();
        // END: com.azure.cosmos.CosmosStoredProcedure.read_no_params
    }

    public void cosmosStoredProcedureReadSample2() {
        String id = "ID";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.read_with_options_param
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

        CosmosStoredProcedureResponse response = procedure.read(options);
        // END: com.azure.cosmos.CosmosStoredProcedure.read_with_options_param
    }

    public void cosmosStoredProcedureDeleteSample1() {
        String id = "ID";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.delete
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);

        CosmosStoredProcedureResponse response = procedure.delete();
        // END: com.azure.cosmos.CosmosStoredProcedure.delete
    }

    public void cosmosStoredProcedureDeleteSample2() {
        String id = "ID";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.delete_with_options_param
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

        CosmosStoredProcedureResponse response = procedure.delete(options);
        // END: com.azure.cosmos.CosmosStoredProcedure.delete_with_options_param
    }

    public void cosmosStoredProcedureExecuteSample() {
        String id = "ID";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.execute
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        List<Object> procedureParams = new ArrayList<>();

        CosmosStoredProcedureResponse response = procedure.execute(procedureParams, options);
        // END: com.azure.cosmos.CosmosStoredProcedure.execute
    }

    public void cosmosStoredProcedureReplaceSample1() {
        String id = "ID";
        String body = "BODY";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_param
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureProperties properties = new CosmosStoredProcedureProperties(id, body);

        CosmosStoredProcedureResponse response = procedure.replace(properties);
        // END: com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_param
    }

    public void cosmosStoredProcedureReplaceSample2() {
        String id = "ID";
        String body = "BODY";
        CosmosAsyncStoredProcedure cosmosAsyncStoredProcedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_and_options_param
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureProperties properties = new CosmosStoredProcedureProperties(id, body);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

        CosmosStoredProcedureResponse response = procedure.replace(properties, options);
        // END: com.azure.cosmos.CosmosStoredProcedure.replace_with_storedProcedureProperties_and_options_param
    }

    public void cosmosTriggerReadSample() {
        String id = "ID";
        CosmosAsyncTrigger cosmosAsyncTrigger = new CosmosAsyncTrigger(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosTrigger.read
        CosmosTrigger trigger = new CosmosTrigger(id, cosmosContainer, cosmosAsyncTrigger);

        CosmosTriggerResponse response = trigger.read();
        // END: com.azure.cosmos.CosmosTrigger.read
    }

    public void cosmosTriggerReplaceSample() {
        String id = "ID";
        String body = "BODY";
        CosmosAsyncTrigger cosmosAsyncTrigger = new CosmosAsyncTrigger(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosTrigger.replace
        CosmosTrigger trigger = new CosmosTrigger(id, cosmosContainer, cosmosAsyncTrigger);
        CosmosTriggerProperties properties = new CosmosTriggerProperties(id, body);

        CosmosTriggerResponse response = trigger.replace(properties);
        // END: com.azure.cosmos.CosmosTrigger.replace
    }

    public void cosmosTriggerDeleteSample() {
        String id = "ID";
        CosmosAsyncTrigger cosmosAsyncTrigger = new CosmosAsyncTrigger(id, cosmosAsyncContainer);
        // BEGIN: com.azure.cosmos.CosmosTrigger.delete
        CosmosTrigger trigger = new CosmosTrigger(id, cosmosContainer, cosmosAsyncTrigger);

        CosmosTriggerResponse response = trigger.delete();
        // END: com.azure.cosmos.CosmosTrigger.delete
    }

    public void cosmosUserGetIdSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.getId
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);

        String userId = user.getId();
        // END: com.azure.cosmos.CosmosUser.getId
    }

    public void cosmosUserReadSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.read
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);

        CosmosUserResponse response = user.read();
        // END: com.azure.cosmos.CosmosUser.read
    }

    public void cosmosUserReplaceSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.replace
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);
        CosmosUserProperties properties = new CosmosUserProperties();

        CosmosUserResponse response = user.replace(properties);
        // END: com.azure.cosmos.CosmosUser.replace
    }

    public void cosmosUserDeleteSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.delete
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);

        CosmosUserResponse response = user.delete();
        // END: com.azure.cosmos.CosmosUser.delete
    }

    public void getDatabaseSample() {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .buildAsyncClient();
        // BEGIN: com.azure.cosmos.CosmosAsyncClient.getDatabase
        String databaseId = "<YOUR DATABASE NAME>";

        CosmosAsyncDatabase database = cosmosAsyncClient.getDatabase(databaseId);
        // END: com.azure.cosmos.CosmosAsyncClient.getDatabase
    }

    public void createGlobalThroughputControlConfigBuilderSample() {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .buildAsyncClient();
        // BEGIN: com.azure.cosmos.CosmosAsyncClient.createGlobalThroughputControlConfigBuilder
        String databaseId = "<YOUR DATABASE NAME>";
        String containerId = "<YOUR CONTAINER NAME>";

        GlobalThroughputControlConfig config =
            cosmosAsyncClient.createGlobalThroughputControlConfigBuilder(databaseId, containerId)
                .build();
        // END: com.azure.cosmos.CosmosAsyncClient.createGlobalThroughputControlConfigBuilder
    }

    public void readConflictSample() {
        CosmosAsyncClient asyncClient = new CosmosClientBuilder().buildAsyncClient();
        // BEGIN: com.azure.cosmos.CosmosAsyncConflict.read
        String conflictId = "CONFLICT_ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(conflictId, asyncClient);
        CosmosAsyncContainer cosmosAsyncContainer =
            new CosmosAsyncContainer(conflictId, cosmosAsyncDatabase);
        CosmosConflictRequestOptions options =
            new CosmosConflictRequestOptions();
        CosmosAsyncConflict conflict =
            new CosmosAsyncConflict(conflictId, cosmosAsyncContainer);

        Mono<CosmosConflictResponse> conflictResponseMono = conflict.read(options);
        // END: com.azure.cosmos.CosmosAsyncConflict.read
    }

    public void CosmosAsyncProcedureReadSample() {
        String id = "ID";
        CosmosAsyncClient client = new CosmosClientBuilder().buildAsyncClient();
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, client);
        // BEGIN: com.azure.cosmos.CosmosAsyncStoredProcedure.read
        CosmosAsyncContainer cosmosAsyncContainer =
            new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosAsyncStoredProcedure procedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);

        Mono<CosmosStoredProcedureResponse> response = procedure.read();
        // END: com.azure.cosmos.CosmosAsyncStoredProcedure.read
    }

    public void CosmosAsyncProcedureDeleteSample() {
        String id = "ID";
        CosmosAsyncClient client = new CosmosClientBuilder().buildAsyncClient();
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, client);
        CosmosAsyncContainer cosmosAsyncContainer =
            new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosAsyncStoredProcedure.delete
        CosmosAsyncStoredProcedure procedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        CosmosStoredProcedureRequestOptions options =
            new CosmosStoredProcedureRequestOptions();

        Mono<CosmosStoredProcedureResponse> response = procedure.delete();
        // END: com.azure.cosmos.CosmosAsyncStoredProcedure.delete
    }

    public void CosmosAsyncProcedureExecuteSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncStoredProcedure.execute
        String id = "ID";
        CosmosAsyncStoredProcedure procedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        CosmosStoredProcedureRequestOptions options =
            new CosmosStoredProcedureRequestOptions();
        List procedureParams = Collections.emptyList();

        Mono<CosmosStoredProcedureResponse> response =
            procedure.execute(procedureParams, options);
        // END: com.azure.cosmos.CosmosAsyncStoredProcedure.execute
    }

    public void CosmosAsyncProcedureReplaceSample() {
        String idTemp = "temp";
        CosmosAsyncClient client = new CosmosClientBuilder().buildAsyncClient();
        // BEGIN: com.azure.cosmos.CosmosAsyncStoredProcedure.replace
        String id = "ID";
        String body = "BODY";
        CosmosAsyncStoredProcedure procedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);
        CosmosStoredProcedureRequestOptions options =
            new CosmosStoredProcedureRequestOptions();
        CosmosStoredProcedureProperties properties =
            new CosmosStoredProcedureProperties(id, body);

        procedure.replace(properties, options);
        // END: com.azure.cosmos.CosmosAsyncStoredProcedure.replace
    }

    public void CosmosAsyncTriggerReadSample() {
        String temp = "ID";
        // BEGIN: com.azure.cosmos.CosmosAsyncTrigger.read
        String id = "ID";
        CosmosAsyncTrigger trigger =
            new CosmosAsyncTrigger(id, cosmosAsyncContainer);

        Mono<CosmosTriggerResponse> response = trigger.read();
        // END: com.azure.cosmos.CosmosAsyncTrigger.read
    }

    public void CosmosAsyncTriggerReplaceSample() {
        String id = "ID";
        String body = "BODY";
        // BEGIN: com.azure.cosmos.CosmosAsyncTrigger.replace
        CosmosAsyncTrigger trigger =
            new CosmosAsyncTrigger(id, cosmosAsyncContainer);
        CosmosTriggerProperties properties =
            new CosmosTriggerProperties(id, body);

        Mono<CosmosTriggerResponse> response =
            trigger.replace(properties);
        // END: com.azure.cosmos.CosmosAsyncTrigger.replace
    }

    public void CosmosAsyncTriggerDeleteSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncTrigger.delete
        String id = "ID";
        CosmosAsyncTrigger trigger =
            new CosmosAsyncTrigger(id, cosmosAsyncContainer);

        Mono<CosmosTriggerResponse> response = trigger.delete();
        // END: com.azure.cosmos.CosmosAsyncTrigger.delete
    }

    public void CosmosAsyncUserReadSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.read
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);

        Mono<CosmosUserResponse> response = user.read();
        // END: com.azure.cosmos.CosmosAsyncUser.read
    }

    public void CosmosAsyncUserReplaceSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.replace
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        CosmosUserProperties properties = new CosmosUserProperties();

        Mono<CosmosUserResponse> response = user.replace(properties);
        // END: com.azure.cosmos.CosmosAsyncUser.replace
    }

    public void CosmosAsyncUserDeleteSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.delete
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);

        Mono<CosmosUserResponse> response = user.delete();
        // END: com.azure.cosmos.CosmosAsyncUser.delete
    }
    public void CosmosAsyncUserCreatePermissionSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.createPermission
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        CosmosPermissionProperties properties =
            new CosmosPermissionProperties();
        CosmosPermissionRequestOptions requestOptions =
            new CosmosPermissionRequestOptions();

        Mono<CosmosPermissionResponse> response =
            user.createPermission(properties, requestOptions);
        // END: com.azure.cosmos.CosmosAsyncUser.createPermission
    }
    public void CosmosAsyncUserUpsertPermissionSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.upsertPermission
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        CosmosPermissionProperties properties =
            new CosmosPermissionProperties();
        CosmosPermissionRequestOptions requestOptions =
            new CosmosPermissionRequestOptions();

        Mono<CosmosPermissionResponse> response =
            user.upsertPermission(properties, requestOptions);
        // END: com.azure.cosmos.CosmosAsyncUser.upsertPermission
    }
    public void CosmosAsyncUserReadPermissionsSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.readAllPermissions
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);

        CosmosPagedFlux<CosmosPermissionProperties> permissions =
            user.readAllPermissions();
        // END: com.azure.cosmos.CosmosAsyncUser.readAllPermissions
    }
    public void CosmosAsyncUserQueryPermissionsSample() {
        String id = "ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.queryPermissions
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        String query = "YOUR_QUERY";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosPermissionProperties> permissions =
            user.queryPermissions(query, options);
        // END: com.azure.cosmos.CosmosAsyncUser.queryPermissions
    }

    public void CosmosAsyncUserFuncReadSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosAsyncUserDefinedFunction.read
        CosmosAsyncUserDefinedFunction userFunction =
            new CosmosAsyncUserDefinedFunction(id, cosmosAsyncContainer);

        Mono<CosmosUserDefinedFunctionResponse> response =
            userFunction.read();
        // END: com.azure.cosmos.CosmosAsyncUserDefinedFunction.read
    }

    public void CosmosAsyncUserFuncReplaceSample() {
        String id = "ID";
        String body = "BODY";
        // BEGIN: com.azure.cosmos.CosmosAsyncUserDefinedFunction.replace
        CosmosAsyncUserDefinedFunction userFunction =
            new CosmosAsyncUserDefinedFunction(id, cosmosAsyncContainer);
        CosmosUserDefinedFunctionProperties properties =
            new CosmosUserDefinedFunctionProperties(id, body);

        Mono<CosmosUserDefinedFunctionResponse> response =
            userFunction.replace(properties);
        // END: com.azure.cosmos.CosmosAsyncUserDefinedFunction.replace
    }

    public void CosmosAsyncUserFuncDeleteSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosAsyncUserDefinedFunction.delete
        CosmosAsyncUserDefinedFunction userFunction =
            new CosmosAsyncUserDefinedFunction(id, cosmosAsyncContainer);

        Mono<CosmosUserDefinedFunctionResponse> response = userFunction.delete();
        // END: com.azure.cosmos.CosmosAsyncUserDefinedFunction.delete
    }

    public void deleteConflictSample() {
        String conflictId = "CONFLICT_ID";
        CosmosAsyncClient asyncClient = new CosmosClientBuilder().buildAsyncClient();
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(conflictId, asyncClient);
        CosmosAsyncContainer cosmosAsyncContainer =
            new CosmosAsyncContainer(conflictId, cosmosAsyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosAsyncConflict.delete
        CosmosConflictRequestOptions options =
            new CosmosConflictRequestOptions();
        CosmosAsyncConflict conflict =
            new CosmosAsyncConflict(conflictId, cosmosAsyncContainer);

        Mono<CosmosConflictResponse> conflictResponseMono = conflict.delete(options);
        // END: com.azure.cosmos.CosmosAsyncConflict.delete
    }

    public void CosmosStoredProcedureReadSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.read
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

        CosmosStoredProcedureResponse response = procedure.read(options);
        // END: com.azure.cosmos.CosmosStoredProcedure.read
    }

    public void CosmosStoredProcedureDeleteSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.delete
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);

        CosmosStoredProcedureResponse response = procedure.delete();
        // END: com.azure.cosmos.CosmosStoredProcedure.delete
    }

    public void CosmosStoredProcedureExecuteSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.execute
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        List<Object> procedureParams = new ArrayList<>();

        CosmosStoredProcedureResponse response = procedure.execute(procedureParams, options);
        // END: com.azure.cosmos.CosmosStoredProcedure.execute
    }

    public void CosmosStoredProcedureReplaceSample() {
        String id = "ID";
        String body = "BODY";
        // BEGIN: com.azure.cosmos.CosmosStoredProcedure.replace
        CosmosStoredProcedure procedure = new CosmosStoredProcedure(id, cosmosContainer, cosmosAsyncStoredProcedure);
        CosmosStoredProcedureProperties properties = new CosmosStoredProcedureProperties(id, body);
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();

        CosmosStoredProcedureResponse response = procedure.replace(properties, options);
        // END: com.azure.cosmos.CosmosStoredProcedure.replace
    }

    public void CosmosTriggerReadSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosTrigger.read
        CosmosTrigger trigger = new CosmosTrigger(id, cosmosContainer, cosmosAsyncTrigger);

        CosmosTriggerResponse response = trigger.read();
        // END: com.azure.cosmos.CosmosTrigger.read
    }

    public void CosmosTriggerReplaceSample() {
        String id = "ID";
        String body = "BODY";
        // BEGIN: com.azure.cosmos.CosmosTrigger.replace
        CosmosTrigger trigger = new CosmosTrigger(id, cosmosContainer, cosmosAsyncTrigger);
        CosmosTriggerProperties properties = new CosmosTriggerProperties(id, body);

        CosmosTriggerResponse response = trigger.replace(properties);
        // END: com.azure.cosmos.CosmosTrigger.replace
    }

    public void CosmosTriggerDeleteSample() {
        String id = "ID";
        // BEGIN: com.azure.cosmos.CosmosTrigger.delete
        CosmosTrigger trigger = new CosmosTrigger(id, cosmosContainer, cosmosAsyncTrigger);

        CosmosTriggerResponse response = trigger.delete();
        // END: com.azure.cosmos.CosmosTrigger.delete
    }

    public void CosmosUserGetIdSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.getId
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);

        String userId = user.getId();
        // END: com.azure.cosmos.CosmosUser.getId
    }

    public void CosmosUserReadSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.read
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);

        CosmosUserResponse response = user.read();
        // END: com.azure.cosmos.CosmosUser.read
    }

    public void CosmosUserReplaceSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.replace
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);
        CosmosUserProperties properties = new CosmosUserProperties();

        CosmosUserResponse response = user.replace(properties);
        // END: com.azure.cosmos.CosmosUser.replace
    }

    public void CosmosUserDeleteSample() {
        String id = "ID";
        CosmosAsyncDatabase asyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosAsyncUser asyncUser = new CosmosAsyncUser(id, asyncDatabase);
        CosmosDatabase cosmosDatabase = new CosmosDatabase(id, cosmosClient, asyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosUser.delete
        CosmosUser user = new CosmosUser(asyncUser, cosmosDatabase, id);

        CosmosUserResponse response = user.delete();
        // END: com.azure.cosmos.CosmosUser.delete
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
