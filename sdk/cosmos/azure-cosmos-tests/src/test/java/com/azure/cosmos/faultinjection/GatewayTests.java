// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosEndToEndOperationLatencyPolicyConfigBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

public class GatewayTests {

    @Test
    public void gatewayFaultInjectionTests() {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .preferredRegions(Arrays.asList("EAST US 2"))
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("SampleDatabase").getContainer("SampleContainer");
        FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("addressRefreshDelay")
            .condition(
                new FaultInjectionConditionBuilder()
                    .region("EAST US 2")
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
//                    .endpoints(
//                        new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey("Test"))).build()
//                    )
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.TOO_MANY_REQUEST)
                    .delay(Duration.ofSeconds(60))
                    .times(1)
                    .build()
            )
            .build();


        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule)).block();

        container.readItem("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f", new PartitionKey("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f"), JsonNode.class)
            .flatMap(response -> {
                System.out.println(response.getDiagnostics());
                return Mono.empty();
            })
            .onErrorResume(throwable -> {
                System.out.println(((CosmosException)throwable).getDiagnostics());
                return Mono.empty();
            })
            .block();
    }

    @Test
    public void regionFailoverTest() throws InterruptedException {

        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        directConnectionConfig.setNetworkRequestTimeout(Duration.ofSeconds(1));

        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .preferredRegions(Arrays.asList("EAST US 2", "West US"))
            .directMode(directConnectionConfig)
            .endToEndOperationLatencyPolicyConfig(new CosmosEndToEndOperationLatencyPolicyConfigBuilder(Duration.ofSeconds(10)).build())
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("TestContainer");
        // first do few reads
        for (int i = 0; i< 10; i++) {

            container.readItem("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f", new PartitionKey("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f"), JsonNode.class)
                .flatMap(response -> {
                    System.out.println(response.getDiagnostics());
                    return Mono.empty();
                })
                .onErrorResume(throwable -> {
                    System.out.println(((CosmosException)throwable).getDiagnostics());
                    return Mono.empty();
                })
                .block();
        }

        System.out.println("Configure fault injection rule");

        FaultInjectionRule addressRefreshFaultInjectionRule = new FaultInjectionRuleBuilder("addressRefreshDelay")
            .condition(
                new FaultInjectionConditionBuilder()
                    .region("EAST US 2")
                    .connectionType(FaultInjectionConnectionType.GATEWAY)
                    .operationType(FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                    .delay(Duration.ofSeconds(60))
                    .build()
            )
            .build();

        FaultInjectionRule responseDelay = new FaultInjectionRuleBuilder("responseDelay")
            .condition(
                new FaultInjectionConditionBuilder()
                    .region("EAST US 2")
                    .operationType(FaultInjectionOperationType.READ_ITEM)
                    .build())
            .result(
                FaultInjectionResultBuilders.getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                    .delay(Duration.ofSeconds(2))
                    .build()
            )
            .build();


        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(addressRefreshFaultInjectionRule, responseDelay)).block();

        System.out.println("Read after rule has been injected");
        container.readItem("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f", new PartitionKey("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f"), JsonNode.class)
            .flatMap(response -> {
                System.out.println(response.getDiagnostics());
                return Mono.empty();
            })
            .onErrorResume(throwable -> {
                System.out.println(((CosmosException)throwable).getDiagnostics());
                return Mono.empty();
            })
            .block();

        Thread.sleep(Duration.ofMinutes(10).toMillis());
    }
}
