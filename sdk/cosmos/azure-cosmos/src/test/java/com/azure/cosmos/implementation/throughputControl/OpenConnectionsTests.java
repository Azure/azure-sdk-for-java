package com.azure.cosmos.implementation.throughputControl;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.time.Duration;

public class OpenConnectionsTests {

    private static final Logger logger = LoggerFactory.getLogger(OpenConnectionsTests.class);

    @Test
    public void openConnectionTests() throws InterruptedException {
        CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
                .endpoint("https://sdk-generic-test-lx.documents.azure.com:443/")
                .key("74KupcHy9yLy5w2ETsBmjmRGBFj1JOEnLy6J83h6SL3Co7yEFGdijHxBSIkyPaj00Grz06sAkrEcZ7dDfYrTug==")
                .buildAsyncClient();

        CosmosAsyncContainer cosmosAsyncContainer = cosmosAsyncClient.getDatabase("TestDataBase").getContainer("TestContainer");
        logger.info("Start to open all connections");
        cosmosAsyncContainer.openConnections().block();
        logger.info("Finished to open all connections");

        Thread.sleep(Duration.ofMinutes(60).toMillis());


//        for (int i = 0; i < 10; i++) {
//            cosmosAsyncContainer.readItem("10", new PartitionKey("10"), JsonNode.class)
//                    .doOnNext(response -> {
//                        logger.info(response.getDiagnostics().toString());
//                    }).block();
//
//            Thread.sleep(Duration.ofMinutes(1).toMillis());
//        }

    }
}
