// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionEndpointBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;

import java.time.Duration;
import java.util.Arrays;

public class ReadmeSamples {
    private final CosmosAsyncClient cosmosAsyncClient = new CosmosClientBuilder()
        .endpoint("<YOUR ENDPOINT HERE>")
        .key("<YOUR KEY HERE>")
        .buildAsyncClient();
    private final CosmosAsyncContainer container =
        cosmosAsyncClient
            .getDatabase("<YOUR DATABASE NAME>")
            .getContainer("<YOUR CONTAINER NAME>");

    public void highChannelAcquisitionScenario() {
        // BEGIN: readme-sample-highChannelAcquisitionScenario
        FaultInjectionRule serverConnectionDelayRule =
            new FaultInjectionRuleBuilder("<YOUR RULE ID>")
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                        .delay(Duration.ofSeconds(6)) // default connection timeout is 5s
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverConnectionDelayRule)).block();
        // END: readme-sample-highChannelAcquisitionScenario
    }

    public void brokenConnectionScenario() {
        // BEGIN: readme-sample-brokenConnectionScenario
        FaultInjectionRule timeoutRule =
            new FaultInjectionRuleBuilder("<YOUR RULE ID>")
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .times(1)
                        .delay(Duration.ofSeconds(6)) // the default time out is 5s
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(timeoutRule)).block();
        // END: readme-sample-brokenConnectionScenario
    }

    public void serverReturnGoneScenario() {
        // BEGIN: readme-sample-serverReturnGoneScenario
        FaultInjectionRule serverErrorRule =
            new FaultInjectionRuleBuilder("<YOUR RULE ID>")
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_ITEM)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serverErrorRule)).block();
        // END: readme-sample-serverReturnGoneScenario
    }

    public void randomConnectionCloseScenario() {
        // BEGIN: readme-sample-randomConnectionCloseScenario
        FaultInjectionRule connectionErrorRule =
            new FaultInjectionRuleBuilder("<YOUR RULE ID>")
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.CREATE_ITEM)
                        .endpoints(new FaultInjectionEndpointBuilder(FeedRange.forLogicalPartition(new PartitionKey("<YOUR PARTITION KEY>"))).build())
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_CLOSE)
                        .interval(Duration.ofSeconds(1))
                        .threshold(1.0)
                        .build()
                )
                .duration(Duration.ofSeconds(2))
                .build();

        CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(connectionErrorRule)).block();
        // END: readme-sample-randomConnectionCloseScenario
    }
}
