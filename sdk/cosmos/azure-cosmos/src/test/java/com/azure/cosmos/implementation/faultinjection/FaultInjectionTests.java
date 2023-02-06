package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.FaultInjectionConditionBuilder;
import com.azure.cosmos.FaultInjectionResultBuilders;
import com.azure.cosmos.FaultInjectionRuleBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.FaultInjectionConnectionErrorType;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionConnectionType;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorType;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

public class FaultInjectionTests {

    @Test
    public void faultInjectionTests() {
        String containerName = "TestContainer2";
        String databaseName = "TestDB";

        CosmosAsyncClient client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(containerName);

        // How to inject a server error
        FaultInjectionRule serverErrorInjectionRule = new FaultInjectionRuleBuilder("serverErrorInjectionRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .operationType(FaultInjectionOperationType.READ)
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region("West US")
                    .endpoints(new FaultInjectionEndpoints(new PartitionKey("SomeValue"), 2, false))
                    .build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionServerErrorType.SERVER_GONE)
                    .times(3)
                    .build())
            .duration(Duration.ofMinutes(30))
            .requestHitLimit(300)
            .enabled(true)
            .build();

        // How to inject connection error
        FaultInjectionRule connectionFaultInjectionRule = new FaultInjectionRuleBuilder("connectionFaultInjectionRule")
            .condition(
                new FaultInjectionConditionBuilder()
                    .connectionType(FaultInjectionConnectionType.DIRECT)
                    .region("West US")
                    .endpoints(
                        new FaultInjectionEndpoints(
                            new PartitionKey("6f65c3ce-ff52-41c1-988f-e25399e9d1bb"),
                            2,
                            false))
                    .build())
            .result(
                FaultInjectionResultBuilders
                    .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_RESET)
                    .interval(Duration.ofMinutes(30))
                    .threshold(0.5)
                    .build())
            .duration(Duration.ofMinutes(30))
            .enabled(true)
            .build();

        container.addFaultInjectionRules(Arrays.asList(serverErrorInjectionRule, connectionFaultInjectionRule)).block();

        System.out.println("Physical addresses for serverErrorInjectionRule: " + serverErrorInjectionRule.getEndpointAddresses());

        Mono.delay(Duration.ofMillis(500))
                .flatMap(t -> {
                    return container.readItem(
                            "6f65c3ce-ff52-41c1-988f-e25399e9d1bb",
                            new PartitionKey("6f65c3ce-ff52-41c1-988f-e25399e9d1bb"),
                            JsonNode.class)
                        .flatMap(response -> {
                            System.out.println(response.getDiagnostics());
                            return Mono.empty();
                        });
                })
                .repeat(2)
                .blockLast();

        serverErrorInjectionRule.enabled(false);
    }
}
