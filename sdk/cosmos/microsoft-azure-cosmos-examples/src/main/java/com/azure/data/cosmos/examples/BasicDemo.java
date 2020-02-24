// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.examples;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosContainerProperties;
import com.azure.data.cosmos.CosmosDatabase;
import com.azure.data.cosmos.CosmosItem;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.CosmosItemResponse;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.FeedResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class BasicDemo {

    private static final String DATABASE_NAME = "test_db";
    private static final String CONTAINER_NAME = "test_container";

    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;

    public static void main(String[] args) {
        BasicDemo demo = new BasicDemo();
        demo.start();
    }

    private void start(){
        // Get client
        client = CosmosClient.builder()
                .endpoint(AccountSettings.HOST)
                .key(AccountSettings.MASTER_KEY)
                .build();

        //CREATE a database and a container
        createDbAndContainerBlocking();

        //Get a proxy reference to container
        container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);

        CosmosContainer container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
        TestObject testObject = new TestObject("item_new_id_1", "test", "test description", "US");
        TestObject testObject2 = new TestObject("item_new_id_2", "test2", "test description2", "CA");

        //CREATE an Item async
        Mono<CosmosItemResponse> itemResponseMono = container.createItem(testObject);
        //CREATE another Item async
        Mono<CosmosItemResponse> itemResponseMono1 = container.createItem(testObject2);

        //Wait for completion
        try {
            itemResponseMono.doOnError(throwable -> log("CREATE item 1", throwable))
                    .mergeWith(itemResponseMono1)
                    .doOnError(throwable -> log("CREATE item 2 ", throwable))
                    .doOnComplete(() -> log("Items created"))
                    .publishOn(Schedulers.elastic())
                    .blockLast();
        }catch (RuntimeException e){
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
        TestObject replaceObject =  new TestObject("item_new_id_3", "test3", "test description3", "JP");
        CosmosItem cosmosItem = null;
        //CREATE item sync
        try {
            cosmosItem = container.createItem(replaceObject)
                    .doOnError(throwable -> log("CREATE 3", throwable))
                    .publishOn(Schedulers.elastic())
                    .block()
                    .item();
        }catch (RuntimeException e){
            log("Couldn't create items due to above exceptions");
        }
        if(cosmosItem != null) {
            replaceObject.setName("new name test3");

            //REPLACE the item and wait for completion
            cosmosItem.replace(replaceObject).block();
        }
    }

    private void createDbAndContainerBlocking() {
        client.createDatabaseIfNotExists(DATABASE_NAME)
                .doOnSuccess(cosmosDatabaseResponse -> log("Database: " + cosmosDatabaseResponse.database().id()))
                .flatMap(dbResponse -> dbResponse.database().createContainerIfNotExists(new CosmosContainerProperties(CONTAINER_NAME, "/country")))
                .doOnSuccess(cosmosContainerResponse -> log("Container: " + cosmosContainerResponse.container().id()))
                .doOnError(throwable -> log(throwable.getMessage()))
                .publishOn(Schedulers.elastic())
                .block();
    }

    int count = 0;
    private void queryItems(){
        log("+ Querying the collection ");
        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.maxDegreeOfParallelism(2);
        Flux<FeedResponse<CosmosItemProperties>> queryFlux = container.queryItems(query, options);

        queryFlux.publishOn(Schedulers.elastic()).subscribe(cosmosItemFeedResponse -> {},
                            throwable -> {},
                            () -> {});

        queryFlux.publishOn(Schedulers.elastic())
                .toIterable()
                .forEach(cosmosItemFeedResponse -> 
                         {
                             log(cosmosItemFeedResponse.results());
                         });

    }
    
    private void queryWithContinuationToken(){
        log("+ Query with paging using continuation token");
        String query = "SELECT * from root r ";
        FeedOptions options = new FeedOptions();
        options.enableCrossPartitionQuery(true);
        options.populateQueryMetrics(true);
        options.maxItemCount(1);
        String continuation = null;
        do{
            options.requestContinuation(continuation);
            Flux<FeedResponse<CosmosItemProperties>> queryFlux = container.queryItems(query, options);
            FeedResponse<CosmosItemProperties> page = queryFlux.blockFirst();
            assert page != null;
            log(page.results());
            continuation = page.continuationToken();
        }while(continuation!= null);

    }
    
    private void log(Object object) {
        System.out.println(object);
    }
    
    private void log(String msg, Throwable throwable){
            log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    class TestObject {
        String id;
        String name;
        String description;
        String country;

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