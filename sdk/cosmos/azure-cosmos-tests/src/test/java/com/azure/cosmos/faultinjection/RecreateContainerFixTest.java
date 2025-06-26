package com.azure.cosmos.faultinjection;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;


public class RecreateContainerFixTest {
    private static final Logger logger = LoggerFactory.getLogger(RecreateContainerFixTest.class);

    @Test
    public void queryWithMetadata() {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("RecreateContainer");
        String query = "select * from c";
        container.queryItems(query, JsonNode.class)
            .byPage()
            .flatMap(response -> {
                System.out.println(response.getCosmosDiagnostics());
                return Mono.empty();
            })
            .blockFirst();
    }


    @Test
    public void queryFullRange() throws InterruptedException {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();

        // create a new container
        logger.info("Creating a new container");
        String containerName = "RecreateContainer";
        String path = "/id";
        int throughput = 10100;
        client
            .getDatabase("TestDatabase")
            .createContainerIfNotExists(containerName, path, ThroughputProperties.createManualThroughput(throughput))
            .block();
        CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer(containerName);

        // create few items
        logger.info("Creating items in the container");
        for (int i = 0; i < 10; i++) {
            container.createItem(TestItem.createNewItem()).block();
        }

        logger.info("Querying items in the container");
        String query = "select * from c";
        container.queryItems(query, JsonNode.class)
            .byPage()
            .blockLast();

        logger.info("Using a different cosmos client to recreate the container");
        CosmosAsyncClient client2 = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildAsyncClient();

        AtomicBoolean shouldExit = new AtomicBoolean(false);
        while (!shouldExit.get()) {
            logger.warn("Trying to simulate");
            client2.getDatabase("TestDatabase").getContainer(containerName).delete().block();

            logger.info("Waiting for the container to be deleted");
            Thread.sleep(Duration.ofMillis(500));

            logger.info("Recreating the container");
            CosmosContainerResponse containerResponse = client2
                    .getDatabase("TestDatabase")
                    .createContainer(containerName, "/id", ThroughputProperties.createManualThroughput(throughput))
                    .block();
            logger.info("New contianer rid {}", containerResponse.getProperties().getResourceId());

            logger.info("Creating few documents using client 2");
            for (int i = 0; i < 10; i++) {
                client2.getDatabase("TestDatabase").getContainer(containerName).createItem(TestItem.createNewItem()).block();
            }

            logger.info("Using client 1 to query items again");
            // scenario 1: request fail with 404/1002 due to incorrect session token being used
            // Direct mode -> first get Invalid Partition key exception, retry, invalid token

            container
                    .queryItems(query, JsonNode.class)
                    .byPage()
                    .onErrorResume(throwable -> {
                        shouldExit.set(true);
                        if (throwable instanceof CosmosException) {
                            logger.warn("Test failed with cosmos exception" + ((CosmosException)throwable).getDiagnostics());
                        } else {
                            logger.warn("Test failed with non-cosmos exception", throwable);
                        }
                        return Mono.empty();
                    })
                    .blockLast();
        }


    }
}
