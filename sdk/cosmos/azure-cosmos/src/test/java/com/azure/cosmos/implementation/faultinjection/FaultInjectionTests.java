package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.FaultInjectionConditionBuilder;
import com.azure.cosmos.FaultInjectionResultBuilders;
import com.azure.cosmos.FaultInjectionRuleBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.FaultInjectionConnectionType;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorType;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

public class FaultInjectionTests {

    @Test
    public void faultInjectionTests() {
        System.setProperty("azure.cosmos.directTcp.defaultOptions", "{\"channelAcquisitionContextEnabled\":\"true\"}");
        String containerName = "TestContainer";
        String databaseName = "TestDatabase";

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(containerName);

//        // To inject a connection latency
//        FaultInjectionRule serverConnectionDelayRule = new FaultInjectionRuleBuilder("serverConnectionDelayRule")
//            .condition(
//                new FaultInjectionConditionBuilder()
//                    .operationType(FaultInjectionOperationType.READ)
//                    .connectionType(FaultInjectionConnectionType.DIRECT)
//                    .endpoints(new FaultInjectionEndpoints(FeedRange.forLogicalPartition(new PartitionKey("1ac44844-b630-436b-bef5-c89d2903da52"))))
//                    .build()
//            )
//            .result(
//                FaultInjectionResultBuilders
//                    .getResultBuilder(FaultInjectionServerErrorType.SERVER_CONNECTION_DELAY)
//                    .times(1)
//                    .delay(Duration.ofSeconds(6))
//                    .build()
//            )
//            .build();
//
//
//        container.configFaultInjectionRules(Arrays.asList(serverConnectionDelayRule)).block();



        // How to inject a server error
        FaultInjectionRule serverErrorInjectionRule = new FaultInjectionRuleBuilder("serverErrorInjectionRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    //.region("West US")
                    .endpoints(new FaultInjectionEndpoints(FeedRange.forLogicalPartition(new PartitionKey("1ac44844-b630-436b-bef5-c89d2903da52")), 4, true))
                    .build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR)
                   // .times(3)
                    .build())
            .duration(Duration.ofMinutes(30))
            .hitLimit(300)
            .enabled(true)
            .build();
//
//        // How to inject connection error
//        FaultInjectionRule connectionFaultInjectionRule = new FaultInjectionRuleBuilder("connectionFaultInjectionRule")
//            .condition(
//                new FaultInjectionConditionBuilder()
//                    .connectionType(FaultInjectionConnectionType.DIRECT)
//                    .region("West US")
//                    .endpoints(
//                        new FaultInjectionEndpoints(
//                            FeedRange.forLogicalPartition(new PartitionKey("6f65c3ce-ff52-41c1-988f-e25399e9d1bb")),
//                            2,
//                            false))
//                    .build())
//            .result(
//                FaultInjectionResultBuilders
//                    .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_RESET)
//                    .interval(Duration.ofMinutes(30))
//                    .threshold(0.5)
//                    .build())
//            .duration(Duration.ofMinutes(30))
//            .enabled(true)
//            .build();

        container.configFaultInjectionRules(Arrays.asList(serverErrorInjectionRule)).block();
        System.out.println(serverErrorInjectionRule.getAddresses());


        // Test to open a new connection
        Flux.range(1, 2)
            .flatMap(t -> {
                return container
                    .readItem("1ac44844-b630-436b-bef5-c89d2903da52", new PartitionKey("1ac44844-b630-436b-bef5-c89d2903da52"), JsonNode.class)
                    .flatMap(response -> {
                        System.out.println(response.getDiagnostics());
                        return Mono.empty();
                    });
            })
            .onErrorResume(throwable -> {
                System.out.println(((CosmosException)throwable).getDiagnostics());
                return Mono.empty();
            })
            .repeat(100)
            .blockLast();
//
//        System.out.println("Physical addresses for serverErrorInjectionRule: " + serverErrorInjectionRule.getAddresses());
//
//        Mono.delay(Duration.ofMillis(500))
//                .flatMap(t -> {
//                    return container.readItem(
//                            "6f65c3ce-ff52-41c1-988f-e25399e9d1bb",
//                            new PartitionKey("6f65c3ce-ff52-41c1-988f-e25399e9d1bb"),
//                            JsonNode.class)
//                        .flatMap(response -> {
//                            System.out.println(response.getDiagnostics());
//                            return Mono.empty();
//                        });
//                })
//                .repeat(2)
//                .blockLast();
//
//        serverErrorInjectionRule.enabled(false);
    }
}
