// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.examples;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.models.QueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class BasicDemo {

    private static final String DATABASE_NAME = "test_db";
    private static final String CONTAINER_NAME = "test_container";
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    public static void main(String[] args) {
        BasicDemo demo = new BasicDemo();
        demo.start();
    }

    private void start() {
        // Get client
        client = new CosmosClientBuilder()
                     .endpoint(AccountSettings.HOST)
                     .key(AccountSettings.MASTER_KEY)
                     .buildAsyncClient();

        //CREATE a database and a container
        createDbAndContainerBlocking();

        //Get a proxy reference to container
        container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);

        CosmosAsyncContainer container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
        TestObject testObject = new TestObject("item_new_id_1", "test", "test description", "US");
        TestObject testObject2 = new TestObject("item_new_id_2", "test2", "test description2", "CA");

        //CREATE an Item async

        Mono<CosmosItemResponse<TestObject>> itemResponseMono = container.createItem(testObject);
        //CREATE another Item async
        Mono<CosmosItemResponse<TestObject>> itemResponseMono1 = container.createItem(testObject2);

        //Wait for completion
        try {
            itemResponseMono.doOnError(throwable -> log("CREATE item 1", throwable))
                .mergeWith(itemResponseMono1)
                .doOnError(throwable -> log("CREATE item 2 ", throwable))
                .doOnComplete(() -> log("Items created"))
                .publishOn(Schedulers.elastic())
                .blockLast();
        } catch (RuntimeException e) {
            log("Couldn't create items due to above exceptions");
        }

        createAndReplaceItem();
        queryItems();
        queryWithContinuationToken();

        //Close client
        client.close();
        log("Completed");
    }

    private void createAndReplaceItem() {
        TestObject replaceObject = new TestObject("item_new_id_3", "test3", "test description3", "JP");
        TestObject properties = null;
        //CREATE item sync
        try {
            properties = container.createItem(replaceObject)
                             .doOnError(throwable -> log("CREATE 3", throwable))
                             .publishOn(Schedulers.elastic())
                             .block()
                             .getItem();
        } catch (RuntimeException e) {
            log("Couldn't create items due to above exceptions");
        }
        if (properties != null) {
            replaceObject.setName("new name test3");

            //REPLACE the item and wait for completion
            container.replaceItem(replaceObject,
                                  properties.getId(),
                                  new PartitionKey(replaceObject.getCountry()))
                .block();
        }
    }

    private void createDbAndContainerBlocking() {

        client.createDatabaseIfNotExists(DATABASE_NAME)
            .doOnSuccess(cosmosDatabaseResponse -> log("Database: " + DATABASE_NAME))
            .flatMap(dbResponse -> client.getDatabase(DATABASE_NAME)
                .createContainerIfNotExists(new CosmosContainerProperties(CONTAINER_NAME,
                    "/country")))
            .doOnSuccess(cosmosContainerResponse -> log("Container: " + CONTAINER_NAME))
            .doOnError(throwable -> log(throwable.getMessage()))
            .publishOn(Schedulers.elastic())
            .block();
    }

    private void queryItems() {
        log("+ Querying the collection ");
        String query = "SELECT * from root";
        QueryRequestOptions options = new QueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        CosmosPagedFlux<TestObject> queryFlux = container.queryItems(query, options, TestObject.class);

        queryFlux.byPage()
                 .publishOn(Schedulers.elastic())
                 .toIterable()
                 .forEach(cosmosItemFeedResponse -> {
                     log(cosmosItemFeedResponse.getResults());
                 });

    }

    private void queryWithContinuationToken() {
        log("+ Query with paging using continuation token");
        String query = "SELECT * from root r ";
        QueryRequestOptions options = new QueryRequestOptions();
        options.setQueryMetricsEnabled(true);
        String continuation = null;
        do {
            CosmosPagedFlux<TestObject> queryFlux = container.queryItems(query, options, TestObject.class);
            FeedResponse<TestObject> page = queryFlux.byPage(continuation, 1).blockFirst();
            assert page != null;
            log(page.getResults());
            continuation = page.getContinuationToken();
        } while (continuation != null);

    }

    private void log(Object object) {
        System.out.println(object);
    }

    private void log(String msg, Throwable throwable) {
        if (throwable instanceof CosmosException) {
            log(msg + ": " + ((CosmosException) throwable).getStatusCode());
        }
    }

    static class TestObject {
        String id;
        String name;
        String description;
        String country;

        public TestObject() {
        }

        public TestObject(String id, String name, String description, String country) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.country = country;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
