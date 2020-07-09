// Disable tests until Cosmos ships from master

// // Copyright (c) Microsoft Corporation. All rights reserved.
// // Licensed under the MIT License.
// package com.azure;

// import com.azure.cosmos.CosmosAsyncClient;
// import com.azure.cosmos.CosmosAsyncContainer;
// import com.azure.cosmos.CosmosAsyncDatabase;
// import com.azure.cosmos.CosmosAsyncItemResponse;
// import com.azure.cosmos.CosmosClientException;
// import com.azure.cosmos.CosmosContainerProperties;
// import com.azure.cosmos.CosmosContinuablePagedFlux;
// import com.azure.cosmos.implementation.CosmosItemProperties;
// import com.azure.cosmos.FeedOptions;
// import com.azure.cosmos.FeedResponse;
// import com.azure.cosmos.PartitionKey;
// import reactor.core.publisher.Mono;
// import reactor.core.scheduler.Schedulers;


// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// import java.util.UUID;

// public class CosmosDB {

//     private static final String DATABASE_NAME = "test_db" + UUID.randomUUID();
//     private static final String CONTAINER_NAME = "test_container";
//     private static CosmosAsyncClient client;
//     private static CosmosAsyncDatabase database;
//     private static CosmosAsyncContainer container;

//     private static final String AZURE_COSMOS_ENDPOINT = System.getenv("AZURE_COSMOS_ENDPOINT");
//     private static final String AZURE_COSMOS_KEY= System.getenv("AZURE_COSMOS_KEY");
//     private static final Logger LOGGER = LoggerFactory.getLogger(CosmosDB.class);

//     public static void main(String[] args) {
//         // Get client
//         client = CosmosAsyncClient.cosmosClientBuilder()
//                      .setEndpoint(AZURE_COSMOS_ENDPOINT)
//                      .setKey(AZURE_COSMOS_KEY)
//                      .buildAsyncClient();

//         //CREATE a database and a container
//         createDbAndContainerBlocking();

//         //Get a proxy reference to container
//         container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);

//         CosmosAsyncContainer container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
//         TestObject testObject = new TestObject("item_new_id_1", "test", "test description", "US");
//         TestObject testObject2 = new TestObject("item_new_id_2", "test2", "test description2", "CA");

//         //CREATE an Item async

//         Mono<CosmosAsyncItemResponse<TestObject>> itemResponseMono = container.createItem(testObject);
//         //CREATE another Item async
//         Mono<CosmosAsyncItemResponse<TestObject>> itemResponseMono1 = container.createItem(testObject2);

//         //Wait for completion
//         itemResponseMono.doOnError(throwable -> LOGGER.info("CREATE item 1", throwable))
//             .mergeWith(itemResponseMono1)
//             .doOnComplete(() -> LOGGER.info("Items created"))
//             .publishOn(Schedulers.elastic())
//             .blockLast();


//         createAndReplaceItem();
//         queryItems();
//         queryWithContinuationToken();

//         deleteDatabaseBlocking();

//         //Close client
//         client.close();
//         LOGGER.info("Completed");
//     }

//     private static void createAndReplaceItem() {
//         TestObject replaceObject = new TestObject("item_new_id_3", "test3", "test description3", "JP");
//         TestObject properties = null;
//         //CREATE item sync
//             properties = container.createItem(replaceObject)
//                              .publishOn(Schedulers.elastic())
//                              .block()
//                              .getResource();

//         if (properties != null) {
//             replaceObject.setName("new name test3");

//             //REPLACE the item and wait for completion
//             container.replaceItem(replaceObject,
//                                   properties.getId(),
//                                   new PartitionKey(replaceObject.getCountry()))
//                 .block();
//         }
//     }

//     private static void createDbAndContainerBlocking() {
//         client.createDatabaseIfNotExists(DATABASE_NAME)
//             .doOnSuccess(cosmosDatabaseResponse -> LOGGER.info("Database: " + cosmosDatabaseResponse.getDatabase().getId()))
//             .flatMap(dbResponse -> dbResponse.getDatabase()
//                                        .createContainerIfNotExists(new CosmosContainerProperties(CONTAINER_NAME,
//                                                                                                  "/country")))
//             .doOnSuccess(cosmosContainerResponse -> LOGGER.info("Container: " + cosmosContainerResponse.getContainer().getId()))
//             .publishOn(Schedulers.elastic())
//             .block();
//     }

//     private static void deleteDatabaseBlocking() {
//         LOGGER.info("Deleting database...");
//         client.getDatabase(DATABASE_NAME).delete().block();
//     }

//     private static void queryItems() {
//         LOGGER.info("+ Querying the collection ");
//         String query = "SELECT * from root";
//         FeedOptions options = new FeedOptions();
//         options.setMaxDegreeOfParallelism(2);
//         CosmosContinuablePagedFlux<TestObject> queryFlux = container.queryItems(query, options, TestObject.class);

//         queryFlux.byPage()
//                  .publishOn(Schedulers.elastic())
//                  .toIterable()
//                  .forEach(cosmosItemFeedResponse -> {
//                      cosmosItemFeedResponse.getResults().forEach(item -> {
//                         LOGGER.info("  - Got Item: {}", item.getId());
//                      });
//                  });
//     }

//     private static void queryWithContinuationToken() {
//         LOGGER.info("+ Query with paging using continuation token");
//         String query = "SELECT * from root r ";
//         FeedOptions options = new FeedOptions();
//         options.populateQueryMetrics(true);
//         options.maxItemCount(1);
//         String continuation = null;
//         do {
//             options.requestContinuation(continuation);
//             CosmosContinuablePagedFlux<TestObject> queryFlux = container.queryItems(query, options, TestObject.class);
//             FeedResponse<TestObject> page = queryFlux.byPage().blockFirst();
//             assert page != null;
//             page.getResults().forEach(item -> {
//                 LOGGER.info("  - Got Item: {}", item.getId());
//             });
//             continuation = page.getContinuationToken();
//         } while (continuation != null);

//     }

//     static class TestObject {
//         String id;
//         String name;
//         String description;
//         String country;

//         public TestObject() {
//         }

//         public TestObject(String id, String name, String description, String country) {
//             this.id = id;
//             this.name = name;
//             this.description = description;
//             this.country = country;
//         }

//         public String getId() {
//             return id;
//         }

//         public void setId(String id) {
//             this.id = id;
//         }

//         public String getCountry() {
//             return country;
//         }

//         public void setCountry(String country) {
//             this.country = country;
//         }

//         public String getName() {
//             return name;
//         }

//         public void setName(String name) {
//             this.name = name;
//         }

//         public String getDescription() {
//             return description;
//         }

//         public void setDescription(String description) {
//             this.description = description;
//         }
//     }
// }