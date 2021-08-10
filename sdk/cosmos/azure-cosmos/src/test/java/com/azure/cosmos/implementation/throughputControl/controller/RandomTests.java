package com.azure.cosmos.implementation.throughputControl.controller;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class RandomTests {
    @Test
    public void diagnositcsTest() throws InterruptedException {
        CosmosAsyncClient client = new CosmosClientBuilder().endpoint("https://cosmos-sdk-tests-3.documents.azure.com:443/")
            .key("ne7c3BVLRk60cqERjIPlllln5I8VSj7b8Y4lRQpXqLR8ikTixzZLsFEXdtKVroD2LfRImqE9MT4lwW8nellQ3w==")
            .buildAsyncClient();

        Flux.range(1, 20)
            .flatMap(t -> client
                .getDatabase("RateLimiterPOC")
                .getContainer("ThrottleDecision")
                .readItem("UmF0ZUxpbWl0ZXJQT0MvZ2xvYmFsRGF0YWJhc2VOYW1lXzE2Mjg1Mzc5NDkzODc=", new PartitionKey("RateLimiterPOC/globalDatabaseName"), JsonNode.class))
            .publishOn(Schedulers.boundedElastic())
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(response -> {
                System.out.println(response.getDiagnostics());
                return Mono.empty();
            })
            .onErrorResume(throwable -> Mono.empty())
            .repeat(3)
            .subscribe();

        Thread.sleep(50000);
    }
}
