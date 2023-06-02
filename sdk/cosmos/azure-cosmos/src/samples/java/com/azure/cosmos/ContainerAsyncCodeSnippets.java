package com.azure.cosmos;

import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ThroughputResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public class ContainerAsyncCodeSnippets {
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

}
