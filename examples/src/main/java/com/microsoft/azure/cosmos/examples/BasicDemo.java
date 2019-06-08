/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos.examples;

import com.microsoft.azure.cosmos.CosmosItem;
import com.microsoft.azure.cosmos.CosmosItemSettings;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosContainer;
import com.microsoft.azure.cosmos.CosmosContainerSettings;
import com.microsoft.azure.cosmos.CosmosDatabase;
import com.microsoft.azure.cosmos.CosmosItemResponse;
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

        //Create a database and a container
        createDbAndContainerBlocking();

        //Get a proxy reference to container
        container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);

        CosmosContainer container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
        TestObject testObject = new TestObject("item_new_id_1", "test", "test description", "US");
        TestObject testObject2 = new TestObject("item_new_id_2", "test2", "test description2", "CA");

        //Create an Item async
        Mono<CosmosItemResponse> itemResponseMono = container.createItem(testObject, testObject.country);
        //Create another Item async
        Mono<CosmosItemResponse> itemResponseMono1 = container.createItem(testObject2, testObject2.country);

        //Wait for completion
        try {
            itemResponseMono.doOnError(throwable -> log("Create item 1", throwable))
                    .mergeWith(itemResponseMono1)
                    .doOnError(throwable -> log("Create item 2 ", throwable))
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
        //Create item sync
        try {
            cosmosItem = container.createItem(replaceObject, replaceObject.country)
                    .doOnError(throwable -> log("Create 3", throwable))
                    .publishOn(Schedulers.elastic())
                    .block()
                    .getItem();
        }catch (RuntimeException e){
            log("Couldn't create items due to above exceptions");
        }
        if(cosmosItem != null) {
            replaceObject.setName("new name test3");

            //Replace the item and wait for completion
            cosmosItem.replace(replaceObject).block();
        }
    }

    private void createDbAndContainerBlocking() {
        client.createDatabaseIfNotExists(DATABASE_NAME)
                .doOnSuccess(cosmosDatabaseResponse -> log("Database: " + cosmosDatabaseResponse.getDatabase().getId()))
                .flatMap(dbResponse -> dbResponse.getDatabase().createContainerIfNotExists(new CosmosContainerSettings(CONTAINER_NAME, "/country")))
                .doOnSuccess(cosmosContainerResponse -> log("Container: " + cosmosContainerResponse.getContainer().getId()))
                .doOnError(throwable -> log(throwable.getMessage()))
                .publishOn(Schedulers.elastic())
                .block();
    }

    int count = 0;
    private void queryItems(){
        log("+ Querying the collection ");
        String query = "SELECT * from root";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setMaxDegreeOfParallelism(2);
        Flux<FeedResponse<CosmosItemSettings>> queryFlux = container.queryItems(query, options);

        queryFlux.publishOn(Schedulers.elastic()).subscribe(cosmosItemFeedResponse -> {},
                            throwable -> {},
                            () -> {});

        queryFlux.publishOn(Schedulers.elastic())
                .toIterable()
                .forEach(cosmosItemFeedResponse -> 
                         {
                             log(cosmosItemFeedResponse.getResults());
                         });

    }
    
    private void queryWithContinuationToken(){
        log("+ Query with paging using continuation token");
        String query = "SELECT * from root r ";
        FeedOptions options = new FeedOptions();
        options.setEnableCrossPartitionQuery(true);
        options.setPopulateQueryMetrics(true);
        options.setMaxItemCount(1);
        String continuation = null;
        do{
            options.setRequestContinuation(continuation);
            Flux<FeedResponse<CosmosItemSettings>> queryFlux = container.queryItems(query, options);
            FeedResponse<CosmosItemSettings> page = queryFlux.blockFirst();
            assert page != null;
            log(page.getResults());
            continuation = page.getResponseContinuation();
        }while(continuation!= null);

    }
    
    private void log(Object object) {
        System.out.println(object);
    }
    
    private void log(String msg, Throwable throwable){
            log(msg + ": " + ((DocumentClientException)throwable).getStatusCode());
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
