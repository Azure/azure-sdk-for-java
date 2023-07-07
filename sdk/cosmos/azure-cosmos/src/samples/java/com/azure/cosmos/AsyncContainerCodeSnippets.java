// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.*;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static com.azure.cosmos.ReadmeSamples.*;

public class AsyncContainerCodeSnippets {
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

    public void getFeedRangesAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.getFeedRanges
        cosmosAsyncContainer.getFeedRanges()
            .subscribe(feedRanges -> {
                for (FeedRange feedRange : feedRanges) {
                    System.out.println("Feed range: " + feedRange);
                }
            });
        // END: com.azure.cosmos.CosmosAsyncContainer.getFeedRanges
    }

    public void getFeedRangesSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.getFeedRanges
        List<FeedRange> feedRanges = cosmosContainer.getFeedRanges();
        for (FeedRange feedRange : feedRanges) {
            System.out.println("Feed range: " + feedRange);
        }
        // END: com.azure.cosmos.CosmosContainer.getFeedRanges
    }

    public void readThroughputAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readThroughput
        Mono<ThroughputResponse> throughputResponseMono = cosmosAsyncContainer.readThroughput();
        throughputResponseMono.subscribe(throughputResponse -> {
            System.out.println(throughputResponse);
        }, throwable -> {
            throwable.printStackTrace();
        });
        // END: com.azure.cosmos.CosmosAsyncContainer.readThroughput
    }

    public void readThroughputSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.readThroughput
        try {
            ThroughputResponse throughputResponse = cosmosContainer.readThroughput();
            System.out.println(throughputResponse);
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosContainer.readThroughput
    }

    public void replaceThroughputAsyncSample() {
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.replaceThroughput
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(1000);

        cosmosAsyncContainer.replaceThroughput(throughputProperties)
            .subscribe(throughputResponse -> {
                    System.out.println(throughputResponse);
                },
                throwable -> {
                    throwable.printStackTrace();
                });
        // END: com.azure.cosmos.CosmosAsyncContainer.replaceThroughput
    }

    public void replaceThroughputSample() {
        // BEGIN: com.azure.cosmos.CosmosContainer.replaceThroughput
        ThroughputProperties throughputProperties =
            ThroughputProperties.createAutoscaledThroughput(1000);
        try {
            ThroughputResponse throughputResponse =
                cosmosContainer.replaceThroughput(throughputProperties);
            System.out.println(throughputResponse);
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosContainer.replaceThroughput
    }

    public void queryConflictsAsyncSample() {
        List<String> conflictIds = Collections.emptyList();
        String query = "SELECT * from c where c.id in (%s)";
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.queryConflicts
        try {
            cosmosAsyncContainer.queryConflicts(query).
                byPage(100)
                .subscribe(response -> {
                    for (CosmosConflictProperties conflictProperties : response.getResults()) {
                        System.out.println(conflictProperties);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosAsyncContainer.queryConflicts
    }

    public void readAllConflictsAsyncSample() {
        List<String> conflictIds = Collections.emptyList();
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.readAllConflicts
        try {
            cosmosAsyncContainer.readAllConflicts(options).
                byPage(100)
                .subscribe(response -> {
                    for (CosmosConflictProperties conflictProperties : response.getResults()) {
                        System.out.println(conflictProperties);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                });
        } catch (CosmosException ce) {
            ce.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // END: com.azure.cosmos.CosmosAsyncContainer.readAllConflicts
    }

    public void patchItemAsyncSample() {
        Passenger passenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.patchItem
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();

        cosmosPatchOperations
            .add("/departure", "SEA")
            .increment("/trips", 1);

        cosmosAsyncContainer.patchItem(
                passenger.getId(),
                new PartitionKey(passenger.getId()),
                cosmosPatchOperations,
                Passenger.class)
            .subscribe(response -> {
                System.out.println(response);
            }, throwable -> {
                throwable.printStackTrace();
            });
        // END: com.azure.cosmos.CosmosAsyncContainer.patchItem
    }

    public void replaceItemAsyncSample() {
        Passenger oldPassenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        Passenger newPassenger = new Passenger("carla.davis@outlook.com", "Carla Davis", "SEA", "IND");
        // BEGIN: com.azure.cosmos.CosmosAsyncContainer.replaceItem
        cosmosAsyncContainer.replaceItem(
                newPassenger,
                oldPassenger.getId(),
                new PartitionKey(oldPassenger.getId()),
                new CosmosItemRequestOptions())
            .subscribe(response -> {
                System.out.println(response);
            }, throwable -> {
                throwable.printStackTrace();
            });
        // END: com.azure.cosmos.CosmosAsyncContainer.replaceItem
    }
}
