// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.ClientEncryptionKey;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;
import reactor.core.publisher.Mono;

import java.util.*;

public class ReadmeSamples {
    private final String serviceEndpoint = "<service-endpoint>";
    private final String key = "<key>";
    private final String id = "<user-id>";
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

    private final CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase("id", cosmosAsyncClient);
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

    public void createDatabaseSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncClient.createDatabase
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .buildAsyncClient();
        String databaseId = "<YOUR DATABASE NAME>";
        CosmosDatabaseProperties databaseProperties =
            new CosmosDatabaseProperties(databaseId);

        Mono<CosmosDatabaseResponse> cosmosDatabaseResponse = cosmosAsyncClient.createDatabase(databaseProperties);
        // END: com.azure.cosmos.CosmosAsyncClient.createDatabase
    }

    public void createDatabaseIfNotExistsSample() {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .buildAsyncClient();
        // BEGIN: com.azure.cosmos.CosmosAsyncClient.createDatabaseIfNotExists
        String databaseId = "<YOUR DATABASE NAME>";

        Mono<CosmosDatabaseResponse> response = cosmosAsyncClient.createDatabaseIfNotExists(databaseId);
        // END: com.azure.cosmos.CosmosAsyncClient.createDatabaseIfNotExists
    }

    public void readAllDatabasesSample() {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .buildAsyncClient();
        // BEGIN: com.azure.cosmos.CosmosAsyncClient.readAllDatabases
        CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();

        cosmosAsyncClient.readAllDatabases(requestOptions);
        // END: com.azure.cosmos.CosmosAsyncClient.readAllDatabases
    }

    public void queryDatabasesSample() {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
            .buildAsyncClient();

        // BEGIN: com.azure.cosmos.CosmosAsyncClient.queryDatabases
        String queryText = "<YOUR QUERY>";
        SqlQuerySpec querySpec = new SqlQuerySpec(queryText);
        CosmosQueryRequestOptions requestOptions =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosDatabaseProperties> dbProperties =
            cosmosAsyncClient.queryDatabases(querySpec, requestOptions);
        // END: com.azure.cosmos.CosmosAsyncClient.queryDatabases
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
        // BEGIN: com.azure.cosmos.CosmosAsyncConflict.read
        String conflictId = "CONFLICT_ID";
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(conflictId, cosmosAsyncClient);
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
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        // BEGIN: com.azure.cosmos.CosmosAsyncStoredProcedure.read
        CosmosAsyncContainer cosmosAsyncContainer =
            new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosAsyncStoredProcedure procedure =
            new CosmosAsyncStoredProcedure(id, cosmosAsyncContainer);

        Mono<CosmosStoredProcedureResponse> response = procedure.read();
        // END: com.azure.cosmos.CosmosAsyncStoredProcedure.read
    }

    public void CosmosAsyncProcedureDeleteSample() {
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
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
        // BEGIN: com.azure.cosmos.CosmosAsyncTrigger.read
        String id = "ID";
        CosmosAsyncTrigger trigger =
            new CosmosAsyncTrigger(id, cosmosAsyncContainer);

        Mono<CosmosTriggerResponse> response = trigger.read();
        // END: com.azure.cosmos.CosmosAsyncTrigger.read
    }

    public void CosmosAsyncTriggerReplaceSample() {
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
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.read
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);

        Mono<CosmosUserResponse> response = user.read();
        // END: com.azure.cosmos.CosmosAsyncUser.read
    }

    public void CosmosAsyncUserReplaceSample() {
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.replace
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        CosmosUserProperties properties = new CosmosUserProperties();
		// END: com.azure.cosmos.CosmosAsyncUser.replace
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

    public void CosmosAsyncUserDeleteSample() {
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.delete
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);

        Mono<CosmosUserResponse> response = user.delete();
        // END: com.azure.cosmos.CosmosAsyncUser.delete
    }

    public void CosmosAsyncUserCreatePermissionSample() {
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
        CosmosAsyncDatabase cosmosAsyncDatabase =
            new CosmosAsyncDatabase(id, new CosmosClientBuilder().buildAsyncClient());
        // BEGIN: com.azure.cosmos.CosmosAsyncUser.readAllPermissions
        CosmosAsyncUser user = new CosmosAsyncUser(id, cosmosAsyncDatabase);

        CosmosPagedFlux<CosmosPermissionProperties> permissions =
            user.readAllPermissions();
        // END: com.azure.cosmos.CosmosAsyncUser.readAllPermissions
    }

    public void CosmosAsyncUserQueryPermissionsSample() {
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
        // BEGIN: com.azure.cosmos.CosmosAsyncUserDefinedFunction.read
        CosmosAsyncUserDefinedFunction userFunction =
            new CosmosAsyncUserDefinedFunction(id, cosmosAsyncContainer);
        // END: com.azure.cosmos.CosmosAsyncUserDefinedFunction.read
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

    public void CosmosAsyncUserFuncReplaceSample() {
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
        // BEGIN: com.azure.cosmos.CosmosAsyncUserDefinedFunction.delete
        CosmosAsyncUserDefinedFunction userFunction =
            new CosmosAsyncUserDefinedFunction(id, cosmosAsyncContainer);
        // END: com.azure.cosmos.CosmosAsyncUserDefinedFunction.delete
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

    public void сosmosAsyncContainerReadSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.read_no_params
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);

        Mono<CosmosContainerResponse> containerResponse = container.read();
        // END: com.azure.cosmos.CosmosAsyncContainer.read_no_params
    }

    public void сosmosAsyncContainerReadSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.read_option_params
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        Mono<CosmosContainerResponse> containerResponse = container.read(options);
        // END: com.azure.cosmos.CosmosAsyncContainer.read_option_params
    }

    public void сosmosAsyncContainerReadThirdSample() {
        Context context = new Context(new Object(), "context");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.read_context_params
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        Mono<CosmosContainerResponse> containerResponse = container.read(options, context);
        // END: com.azure.cosmos.CosmosAsyncContainer.read_context_params
    }

    public void сosmosAsyncContainerDeleteSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.delete_no_params
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);

        Mono<CosmosContainerResponse> containerResponse = container.delete();
        // END: com.azure.cosmos.CosmosAsyncContainer.delete_no_params
    }

    public void сosmosAsyncContainerDeleteSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.delete_option_params
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();

        Mono<CosmosContainerResponse> containerResponse = container.delete(options);
        // END: com.azure.cosmos.CosmosAsyncContainer.delete_option_params
    }

    public void сosmosAsyncContainerReplaceSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.replace_properties_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);
        Mono<CosmosContainerResponse> containerResponse = container.replace(properties);
        // END: com.azure.cosmos.CosmosAsyncContainer.replace_properties_params
    }

    public void сosmosAsyncContainerReplaceSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.replace_option_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerRequestOptions options = new CosmosContainerRequestOptions();
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);
        Mono<CosmosContainerResponse> containerResponse = container.replace(properties, options);
        // END: com.azure.cosmos.CosmosAsyncContainer.replace_option_params
    }

    public void сosmosAsyncContainerCreateItemSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.createItem_no_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);

        Mono<CosmosItemResponse<CosmosContainerProperties>> containerResponse =
            container.createItem(properties);
        // END: com.azure.cosmos.CosmosAsyncContainer.createItem_no_params
    }

    public void сosmosAsyncContainerCreateItemSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.createItem_two_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        Mono<CosmosItemResponse<CosmosContainerProperties>> containerResponse =
            container.createItem(properties, options);
        // END: com.azure.cosmos.CosmosAsyncContainer.createItem_two_params
    }

    public void сosmosAsyncContainerCreateItemThirdSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.createItem_three_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        PartitionKey partitionKey = new PartitionKey(partitionKeyPath);
        CosmosAsyncContainer container = new CosmosAsyncContainer(id, cosmosAsyncDatabase);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();

        Mono<CosmosItemResponse<CosmosContainerProperties>> containerResponse =
            container.createItem(properties, partitionKey, options);
        // END: com.azure.cosmos.CosmosAsyncContainer.createItem_three_params
    }

    public void сosmosAsyncDatabaseContainerIfNotExistsSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_properties_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);

        Mono<CosmosContainerResponse> response =
            cosmosAsyncDatabase.createContainerIfNotExists(properties);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_properties_params
    }

    public void сosmosAsyncDatabaseContainerIfNotExistsSecondSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_throughput_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);

        Mono<CosmosContainerResponse> response =
            cosmosAsyncDatabase.createContainerIfNotExists(properties, throughput);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_throughput_params
    }

    public void сosmosAsyncDatabaseContainerIfNotExistsThirdSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_throughputproperties_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(throughput);

        Mono<CosmosContainerResponse> response =
            cosmosAsyncDatabase.createContainerIfNotExists(properties, throughputProperties);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_throughputproperties_params
    }

    public void сosmosAsyncDatabaseContainerIfNotExistsFourthSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_partitionkeypath_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);

        Mono<CosmosContainerResponse> response =
            cosmosAsyncDatabase.createContainerIfNotExists(id, partitionKeyPath);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_partitionkeypath_params
    }

    public void сosmosAsyncDatabaseContainerIfNotExistsFifthSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_id_partitionKeyPath_throughputProperties_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(throughput);

        Mono<CosmosContainerResponse> response =
            cosmosAsyncDatabase.createContainerIfNotExists(id, partitionKeyPath, throughputProperties);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_id_partitionKeyPath_throughputProperties_params
    }

    public void сosmosAsyncDatabaseContainerIfNotExistsSixthSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_properties_through_params
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosContainerProperties properties = new CosmosContainerProperties(id, partitionKeyPath);

        Mono<CosmosContainerResponse> response =
            cosmosAsyncDatabase.createContainerIfNotExists(properties, throughput);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createContainerIfNotExists_properties_through_params
    }

    public void сosmosAsyncDatabaseReadAllContainersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllContainers_option_params
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosContainerProperties> containerProperties =
            cosmosAsyncDatabase.readAllContainers(options);
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllContainers_option_params
    }

    public void сosmosAsyncDatabaseReadAllContainersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllContainers_no_params
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);

        CosmosPagedFlux<CosmosContainerProperties> containerProperties =
            cosmosAsyncDatabase.readAllContainers();
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllContainers_no_params
    }

    public void сosmosAsyncDatabaseQueryContainersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_query_params
        String query = "<YOUR_QUERY>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);

        CosmosPagedFlux<CosmosContainerProperties> containerProperties =
            cosmosAsyncDatabase.queryContainers(query);
        // END: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_query_params
    }

    public void сosmosAsyncDatabaseQueryContainersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_spec_option_params
        String query = "<YOUR_QUERY>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        SqlQuerySpec spec = new SqlQuerySpec(query);

        CosmosPagedFlux<CosmosContainerProperties> containerProperties =
            cosmosAsyncDatabase.queryContainers(spec, options);
        // END: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_spec_option_params
    }

    public void сosmosAsyncDatabaseQueryContainersThirdSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_option_params
        String query = "<YOUR_QUERY>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosContainerProperties> containerProperties =
            cosmosAsyncDatabase.queryContainers(query, options);
        // END: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_option_params
    }

    public void сosmosAsyncDatabaseQueryContainersFourthSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_spec_params
        String query = "<YOUR_QUERY>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        SqlQuerySpec spec = new SqlQuerySpec(query);

        CosmosPagedFlux<CosmosContainerProperties> containerProperties =
            cosmosAsyncDatabase.queryContainers(spec);
        // END: com.azure.cosmos.CosmosAsyncDatabase.queryContainers_spec_params
    }

    public void сosmosAsyncDatabaseCreateUserSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createUser
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosUserProperties properties = new CosmosUserProperties();
        Mono<CosmosUserResponse> userResponse = cosmosAsyncDatabase.createUser(properties);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createUser
    }
    public void сosmosAsyncDatabaseUpsertUserSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.upsertUser
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosUserProperties properties = new CosmosUserProperties();
        Mono<CosmosUserResponse> userResponse = cosmosAsyncDatabase.upsertUser(properties);
        // END: com.azure.cosmos.CosmosAsyncDatabase.upsertUser
    }
    public void сosmosAsyncDatabaseCreateClientKeySample() {
        String encryptionAlgorithm = "";
        byte[] wrappedDataEncryptionKey = new byte[]{};
        EncryptionKeyWrapMetadata metadata = new EncryptionKeyWrapMetadata();
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.createClientEncryptionKey
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        ClientEncryptionKey key = new ClientEncryptionKey();
        CosmosClientEncryptionKeyProperties properties =
            new CosmosClientEncryptionKeyProperties(id, encryptionAlgorithm, wrappedDataEncryptionKey, metadata);
        Mono<CosmosClientEncryptionKeyResponse> keyProperties =
            cosmosAsyncDatabase.createClientEncryptionKey(properties);
        // END: com.azure.cosmos.CosmosAsyncDatabase.createClientEncryptionKey
    }

    public void сosmosAsyncDatabaseReadAllUsersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllUsers_no_params
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);

        CosmosPagedFlux<CosmosUserProperties> userProperties =
            cosmosAsyncDatabase.readAllUsers();
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllUsers_no_params
    }

    public void сosmosAsyncDatabaseReadAllUsersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllUsers_option_params
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosUserProperties> userProperties =
            cosmosAsyncDatabase.readAllUsers(queryRequestOptions);
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllUsers_option_params
    }

    public void сosmosAsyncDatabaseReadAllClientEncryptionKeysSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllClientEncryptionKeys_no_params
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);

        CosmosPagedFlux<CosmosClientEncryptionKeyProperties> keyProperties =
            cosmosAsyncDatabase.readAllClientEncryptionKeys();
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllClientEncryptionKeys_no_params
    }

    public void сosmosAsyncDatabaseReadAllClientEncryptionKeysSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.readAllClientEncryptionKeys_option_params
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosClientEncryptionKeyProperties> keyProperties =
            cosmosAsyncDatabase.readAllClientEncryptionKeys(options);
        // END: com.azure.cosmos.CosmosAsyncDatabase.readAllClientEncryptionKeys_option_params
    }

    public void сosmosAsyncDatabaseQueryClientKeysSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncDatabase.queryClientEncryptionKeys
        String query = "<YOUR_QUERY>";
        CosmosAsyncDatabase cosmosAsyncDatabase = new CosmosAsyncDatabase(id, cosmosAsyncClient);
        CosmosQueryRequestOptions requestOptions = new CosmosQueryRequestOptions();
        CosmosPagedFlux<CosmosClientEncryptionKeyProperties> clientEncryptionKeys =
            cosmosAsyncDatabase.queryClientEncryptionKeys(query, requestOptions);
        // END: com.azure.cosmos.CosmosAsyncDatabase.queryClientEncryptionKeys
    }

    public void сosmosAsyncPermissionReadSample() {
        CosmosAsyncUser cosmosAsyncUser = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosAsyncPermission.read
        CosmosAsyncPermission permission = new CosmosAsyncPermission(id, cosmosAsyncUser);
        CosmosPermissionRequestOptions options = new CosmosPermissionRequestOptions();

        Mono<CosmosPermissionResponse> permissionResponse = permission.read(options);
        // END: com.azure.cosmos.CosmosAsyncPermission.read
    }

    public void сosmosAsyncPermissionReplaceSample() {
        CosmosAsyncUser cosmosAsyncUser = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosAsyncPermission.replace
        CosmosAsyncPermission permission = new CosmosAsyncPermission(id, cosmosAsyncUser);
        CosmosPermissionProperties properties = new CosmosPermissionProperties();
        CosmosPermissionRequestOptions options = new CosmosPermissionRequestOptions();

        Mono<CosmosPermissionResponse> permissionResponse = permission.replace(properties, options);
        // END: com.azure.cosmos.CosmosAsyncPermission.replace
    }

    public void сosmosAsyncPermissionDeleteSample() {
        CosmosAsyncUser cosmosAsyncUser = new CosmosAsyncUser(id, cosmosAsyncDatabase);
        // BEGIN: com.azure.cosmos.CosmosAsyncPermission.delete
        CosmosAsyncPermission permission = new CosmosAsyncPermission(id, cosmosAsyncUser);
        CosmosPermissionRequestOptions options = new CosmosPermissionRequestOptions();

        Mono<CosmosPermissionResponse> permissionResponse = permission.delete(options);
        // END: com.azure.cosmos.CosmosAsyncPermission.delete
    }

    public void cosmosAsyncScriptsQueryTriggersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.queryTriggers_option_params
        String query = "<YOUR_QUERY>";
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosTriggerProperties> triggerProperties =
            asyncScripts.queryTriggers(query, options);
        // END: com.azure.cosmos.CosmosAsyncScripts.queryTriggers_option_params
    }

    public void cosmosAsyncScriptsQueryTriggersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.queryTriggers_spec_params
        String query = "<YOUR_QUERY>";
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();
        SqlQuerySpec spec = new SqlQuerySpec(query);

        CosmosPagedFlux<CosmosTriggerProperties> triggerProperties =
            asyncScripts.queryTriggers(spec, options);
        // END: com.azure.cosmos.CosmosAsyncScripts.queryTriggers_spec_params
    }

    public void cosmosAsyncScriptsCreateStoredProcedureSample() {
        String body = "";
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.createStoredProcedure
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosStoredProcedureProperties properties =
            new CosmosStoredProcedureProperties(id, body);
        CosmosStoredProcedureRequestOptions options =
            new CosmosStoredProcedureRequestOptions();

        Mono<CosmosStoredProcedureResponse> response =
            asyncScripts.createStoredProcedure(properties, options);
        // END: com.azure.cosmos.CosmosAsyncScripts.createStoredProcedure
    }

    public void cosmosAsyncScriptsReadAllStoredProceduresSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.readAllStoredProcedures
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosStoredProcedureProperties> properties =
            asyncScripts.readAllStoredProcedures(options);
        // END: com.azure.cosmos.CosmosAsyncScripts.readAllStoredProcedures
    }

    public void cosmosAsyncScriptsQueryStoredProceduresSample() {
        String query = "";
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.queryStoredProcedures
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosStoredProcedureProperties> properties =
            asyncScripts.queryStoredProcedures(query, options);
        // END: com.azure.cosmos.CosmosAsyncScripts.queryStoredProcedures
    }

    public void cosmosAsyncScriptsCreateUserFunctionSample() {
        String body = "";
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.createUserDefinedFunction
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosUserDefinedFunctionProperties properties =
            new CosmosUserDefinedFunctionProperties(id, body);

        Mono<CosmosUserDefinedFunctionResponse> procedureProperties =
            asyncScripts.createUserDefinedFunction(properties);
        // END: com.azure.cosmos.CosmosAsyncScripts.createUserDefinedFunction
    }

    public void cosmosAsyncScriptsReadAllUserFunctionsSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.readAllUserDefinedFunctions
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosUserDefinedFunctionProperties> properties =
            asyncScripts.readAllUserDefinedFunctions(options);
        // END: com.azure.cosmos.CosmosAsyncScripts.readAllUserDefinedFunctions
    }

    public void cosmosAsyncScriptsQueryUserDefinedFunctionsSample() {
        String query = "";
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.queryUserDefinedFunctions
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosUserDefinedFunctionProperties> properties =
            asyncScripts.queryUserDefinedFunctions(query, options);
        // END: com.azure.cosmos.CosmosAsyncScripts.queryUserDefinedFunctions
    }

    public void cosmosAsyncScriptsCreateTriggerSample() {
        String body = "";
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.createTrigger
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosTriggerProperties properties =
            new CosmosTriggerProperties(id, body);

        Mono<CosmosTriggerResponse> triggerResponse =
            asyncScripts.createTrigger(properties);
        // END: com.azure.cosmos.CosmosAsyncScripts.createTrigger
    }

    public void cosmosAsyncScriptsReadAllTriggersSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.readAllTriggers_no_params
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);

        CosmosPagedFlux<CosmosTriggerProperties> triggerProperties =
            asyncScripts.readAllTriggers();
        // END: com.azure.cosmos.CosmosAsyncScripts.readAllTriggers_no_params
    }

    public void cosmosAsyncScriptsReadAllTriggersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncScripts.readAllTriggers_option_params
        CosmosAsyncScripts asyncScripts =
            new CosmosAsyncScripts(cosmosAsyncContainer);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedFlux<CosmosTriggerProperties> triggerProperties =
            asyncScripts.readAllTriggers(options);
        // END: com.azure.cosmos.CosmosAsyncScripts.readAllTriggers_option_params
    }

    public void cosmosDatabaseReadSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.read_no_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosDatabaseResponse response = cosmosDatabase.read();
        // END: com.azure.cosmos.CosmosDatabase.read_no_params
    }

    public void cosmosDatabaseReadSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.read_option_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosDatabaseRequestOptions options =
            new CosmosDatabaseRequestOptions();

        CosmosDatabaseResponse response = cosmosDatabase.read(options);
        // END: com.azure.cosmos.CosmosDatabase.read_option_params
    }

    public void cosmosDatabaseDeleteSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.delete_option_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosDatabaseRequestOptions options =
            new CosmosDatabaseRequestOptions();

        CosmosDatabaseResponse response = cosmosDatabase.delete(options);
        // END: com.azure.cosmos.CosmosDatabase.delete_option_params
    }

    public void cosmosDatabaseCreateContainerIfNotExistsSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_prop
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosContainerProperties properties =
            new CosmosContainerProperties(id, partitionKeyPath);

        CosmosContainerResponse response =
            cosmosDatabase.createContainerIfNotExists(properties);
        // END: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_prop
    }

    public void cosmosDatabaseCreateContainerIfNotExistsSecondSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_prop_through
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosContainerProperties properties =
            new CosmosContainerProperties(id, partitionKeyPath);

        CosmosContainerResponse response =
            cosmosDatabase.createContainerIfNotExists(properties, throughput);
        // END: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_prop_through
    }

    public void cosmosDatabaseCreateContainerIfNotExistsThirdSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_prop_throughprop
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosContainerProperties properties =
            new CosmosContainerProperties(id, partitionKeyPath);
        ThroughputProperties throughputProperties =
            ThroughputProperties.createManualThroughput(throughput);

        CosmosContainerResponse response =
            cosmosDatabase.createContainerIfNotExists(properties, throughputProperties);
        // END: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_prop_throughprop
    }

    public void cosmosDatabaseCreateContainerIfNotExistsFourthSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_id_partitionKey
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosContainerResponse response =
            cosmosDatabase.createContainerIfNotExists(id, partitionKeyPath);
        // END: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_id_partitionKey
    }

    public void cosmosDatabaseCreateContainerIfNotExistsFifthSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_id_partitionKey_through
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosContainerResponse response =
            cosmosDatabase.createContainerIfNotExists(id, partitionKeyPath, throughput);
        // END: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_id_partitionKey_through
    }


    public void cosmosDatabaseCreateContainerIfNotExistsSixthSample() {
        int throughput = 1;
        // BEGIN: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_id_partitionKey_throughProperties
        String partitionKeyPath = "<PARTITION_KEY_PATH>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        ThroughputProperties throughputProperties =
            ThroughputProperties.createManualThroughput(throughput);

        CosmosContainerResponse response =
            cosmosDatabase.createContainerIfNotExists(id, partitionKeyPath, throughputProperties);
        // END: com.azure.cosmos.CosmosDatabase.createContainerIfNotExists_id_partitionKey_throughProperties
    }


    public void cosmosDatabaseReadAllContainersSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.readAllContainers
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosPagedIterable<CosmosContainerProperties> containerProperties =
            cosmosDatabase.readAllContainers();
        // END: com.azure.cosmos.CosmosDatabase.readAllContainers
    }

    public void cosmosDatabaseQueryContainersSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryContainers
        String query = "<YOUR_QUERY>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosContainerProperties> containerProperties =
            cosmosDatabase.queryContainers(query, options);
        // END: com.azure.cosmos.CosmosDatabase.queryContainers
    }

    public void cosmosDatabaseQueryContainersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryContainers_query_params
        String query = "<YOUR_QUERY>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosPagedIterable<CosmosContainerProperties> containerProperties =
            cosmosDatabase.queryContainers(query);
        // END: com.azure.cosmos.CosmosDatabase.queryContainers_query_params
    }

    public void cosmosDatabaseQueryContainersThirdSample() {
        String query = "<YOUR_QUERY>";
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryContainers_spec_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        SqlQuerySpec spec =
            new SqlQuerySpec(query);

        CosmosPagedIterable<CosmosContainerProperties> containerProperties =
            cosmosDatabase.queryContainers(spec);
        // END: com.azure.cosmos.CosmosDatabase.queryContainers_spec_params
    }

    public void cosmosDatabaseQueryContainersFourthSample() {
        String query = "<YOUR_QUERY>";
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryContainers_spec_option_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();
        SqlQuerySpec spec =
            new SqlQuerySpec(query);

        CosmosPagedIterable<CosmosContainerProperties> containerProperties =
            cosmosDatabase.queryContainers(spec, options);
        // END: com.azure.cosmos.CosmosDatabase.queryContainers_spec_option_params
    }

    public void cosmosDatabaseGetContainerSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.getContainer
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosContainer cosmosContainer = cosmosDatabase.getContainer(id);
        // END: com.azure.cosmos.CosmosDatabase.getContainer
    }

    public void cosmosDatabaseCreateUserSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.createUser
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosUserProperties properties =
            new CosmosUserProperties();

        CosmosUserResponse response = cosmosDatabase.createUser(properties);
        // END: com.azure.cosmos.CosmosDatabase.createUser
    }

    public void cosmosDatabaseUpsertUserSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.upsertUser
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosUserProperties properties =
            new CosmosUserProperties();

        CosmosUserResponse response = cosmosDatabase.upsertUser(properties);
        // END: com.azure.cosmos.CosmosDatabase.upsertUser
    }

    public void cosmosDatabaseReadAllUsersSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.readAllUsers_no_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosPagedIterable<CosmosUserProperties> userProperties =
            cosmosDatabase.readAllUsers();
        // END: com.azure.cosmos.CosmosDatabase.readAllUsers_no_params
    }

    public void cosmosDatabaseReadAllUsersSecondSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.readAllUsers_option_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosUserProperties> userProperties =
            cosmosDatabase.readAllUsers(options);
        // END: com.azure.cosmos.CosmosDatabase.readAllUsers_option_params
    }

    public void cosmosDatabaseQueryUsersSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryUsers
        String query = "<YOUR_QUERY>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosPagedIterable<CosmosUserProperties> userProperties =
            cosmosDatabase.queryUsers(query);
        // END: com.azure.cosmos.CosmosDatabase.queryUsers
    }

    public void cosmosDatabaseQueryUsersSecondSample() {
        String query = "<YOUR_QUERY>";
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryUsers_spec_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        SqlQuerySpec spec = new SqlQuerySpec(query);

        CosmosPagedIterable<CosmosUserProperties> userProperties =
            cosmosDatabase.queryUsers(spec);
        // END: com.azure.cosmos.CosmosDatabase.queryUsers_spec_params
    }

    public void cosmosDatabaseQueryUsersThirdSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryUsers_option_params
        String query = "<YOUR_QUERY>";
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosUserProperties> userProperties =
            cosmosDatabase.queryUsers(query, options);
        // END: com.azure.cosmos.CosmosDatabase.queryUsers_option_params
    }

    public void cosmosDatabaseQueryUsersFourthSample() {
        String query = "<YOUR_QUERY>";
        // BEGIN: com.azure.cosmos.CosmosDatabase.queryUsers_two_params
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        SqlQuerySpec spec = new SqlQuerySpec(query);
        CosmosQueryRequestOptions options =
            new CosmosQueryRequestOptions();

        CosmosPagedIterable<CosmosUserProperties> userProperties =
            cosmosDatabase.queryUsers(spec, options);
        // END: com.azure.cosmos.CosmosDatabase.queryUsers_two_params
    }

    public void cosmosDatabaseReplaceThroughputSample() {
        int throughputId = 0;
        // BEGIN: com.azure.cosmos.CosmosDatabase.replaceThroughput
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);
        ThroughputProperties properties =
            ThroughputProperties.createManualThroughput(throughputId);

        ThroughputResponse response = cosmosDatabase.replaceThroughput(properties);
        // END: com.azure.cosmos.CosmosDatabase.replaceThroughput
    }

    public void cosmosDatabaseGetClientKeySample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.getClientEncryptionKey
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosClientEncryptionKey clientEncryptionKey =
            cosmosDatabase.getClientEncryptionKey(id);
        // END: com.azure.cosmos.CosmosDatabase.getClientEncryptionKey
    }

    public void cosmosDatabaseReadAllClientKeysSample() {
        // BEGIN: com.azure.cosmos.CosmosDatabase.readAllClientEncryptionKeys
        CosmosDatabase cosmosDatabase =
            new CosmosDatabase(id, cosmosClient, cosmosAsyncDatabase);

        CosmosPagedIterable<CosmosClientEncryptionKeyProperties> response =
            cosmosDatabase.readAllClientEncryptionKeys();
        // END: com.azure.cosmos.CosmosDatabase.readAllClientEncryptionKeys
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
