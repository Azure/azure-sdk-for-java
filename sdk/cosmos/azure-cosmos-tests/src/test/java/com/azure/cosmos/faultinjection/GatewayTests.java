// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
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

import java.time.Duration;
import java.util.Arrays;

public class GatewayTests {

    @Test
    public void gatewayFaultInjectionTests() {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("TestDatabase").getContainer("TestContainer");
        FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder("addressRefreshDelay")
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

        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(faultInjectionRule)).block();

        container.readItem("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f", new PartitionKey("ee7f3ac5-5761-492a-aad9-5493f0b0ad3f"), JsonNode.class)
            .block();
    }
}
