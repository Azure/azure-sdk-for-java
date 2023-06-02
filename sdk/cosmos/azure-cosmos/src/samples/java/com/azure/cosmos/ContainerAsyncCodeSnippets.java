package com.azure.cosmos;

import com.azure.cosmos.models.FeedRange;

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

}
