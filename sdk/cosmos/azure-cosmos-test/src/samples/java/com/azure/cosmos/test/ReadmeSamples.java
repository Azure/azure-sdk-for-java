// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
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

    /**
     * Demonstrates fault injection for response delay using GATEWAY V2 and HTTP/2 configuration.
     */
    public void responseDelayWithGatewayV2Scenario() {
        // BEGIN: readme-sample-responseDelayWithGatewayV2Scenario
        // Enable thin client and configure HTTP/2
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        CosmosAsyncClient gatewayV2AsyncClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .contentResponseOnWriteEnabled(true)
            .gatewayMode(new GatewayConnectionConfig().setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true)))
            .buildAsyncClient();

        CosmosAsyncContainer container = gatewayV2AsyncClient
            .getDatabase("<YOUR DATABASE NAME>")
            .getContainer("<YOUR CONTAINER NAME>");

        // Define fault injection rule for response delay
        FaultInjectionRule responseDelayRule = new FaultInjectionRuleBuilder("response-delay-rule")
            .condition(new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .connectionType(FaultInjectionConnectionType.GATEWAY)
                .build())
            .result(FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                .delay(Duration.ofSeconds(10))
                .times(1)
                .build())
            .duration(Duration.ofMinutes(5))
            .build();

        try {
            // Apply fault injection rule
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(responseDelayRule)).block();

            // Trigger fault injection by performing a read operation
            container.readItem("<ITEM_ID>", new PartitionKey("<PARTITION_KEY>"), Object.class).block();
        } finally {
            // Clean up
            responseDelayRule.disable();
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
            gatewayV2AsyncClient.close();
        }
        // END: readme-sample-responseDelayWithGatewayV2Scenario
    }

    /**
     * Demonstrates fault injection for connection delay using GATEWAY V2 and HTTP/2 configuration.
     */
    public void connectionDelayWithGatewayV2Scenario() {
        // BEGIN: readme-sample-connectionDelayWithGatewayV2Scenario
        // Enable thin client and configure HTTP/2
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        CosmosAsyncClient gatewayV2AsyncClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .contentResponseOnWriteEnabled(true)
            .gatewayMode(new GatewayConnectionConfig().setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true)))
            .buildAsyncClient();

        CosmosAsyncContainer container = gatewayV2AsyncClient
            .getDatabase("<YOUR DATABASE NAME>")
            .getContainer("<YOUR CONTAINER NAME>");

        // Define fault injection rule for connection delay
        FaultInjectionRule connectionDelayRule = new FaultInjectionRuleBuilder("connection-delay-rule")
            .condition(new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .connectionType(FaultInjectionConnectionType.GATEWAY)
                .build())
            .result(FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                .delay(Duration.ofSeconds(8))
                .times(1)
                .build())
            .duration(Duration.ofMinutes(5))
            .build();

        try {
            // Apply fault injection rule
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(connectionDelayRule)).block();

            // Trigger fault injection by performing a read operation
            container.readItem("<ITEM_ID>", new PartitionKey("<PARTITION_KEY>"), Object.class).block();
        } finally {
            // Clean up
            connectionDelayRule.disable();
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
            gatewayV2AsyncClient.close();
        }
        // END: readme-sample-connectionDelayWithGatewayV2Scenario
    }

    /**
     * Demonstrates fault injection for service unavailable using GATEWAY V2 and HTTP/2 configuration.
     */
    public void serviceUnavailableWithGatewayV2Scenario() {
        // BEGIN: readme-sample-serviceUnavailableWithGatewayV2Scenario
        // Enable thin client and configure HTTP/2
        System.setProperty("COSMOS.THINCLIENT_ENABLED", "true");

        CosmosAsyncClient gatewayV2AsyncClient = new CosmosClientBuilder()
            .endpoint("<YOUR ENDPOINT HERE>")
            .key("<YOUR KEY HERE>")
            .gatewayMode(new GatewayConnectionConfig().setHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(true)))
            .contentResponseOnWriteEnabled(true)
            .buildAsyncClient();

        CosmosAsyncContainer container = gatewayV2AsyncClient
            .getDatabase("<YOUR DATABASE NAME>")
            .getContainer("<YOUR CONTAINER NAME>");

        // Define fault injection rule for service unavailable
        FaultInjectionRule serviceUnavailableRule = new FaultInjectionRuleBuilder("service-unavailable-rule")
            .condition(new FaultInjectionConditionBuilder()
                .operationType(FaultInjectionOperationType.READ_ITEM)
                .connectionType(FaultInjectionConnectionType.GATEWAY)
                .build())
            .result(FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE)
                .times(1)
                .build())
            .duration(Duration.ofMinutes(5))
            .build();

        try {
            // Apply fault injection rule
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(serviceUnavailableRule)).block();

            try {
                // Trigger fault injection by performing a read operation
                container.readItem("<ITEM_ID>", new PartitionKey("<PARTITION_KEY>"), Object.class).block();
            } catch (CosmosException e) {
                // Log diagnostics if fault injection causes failure
                CosmosDiagnostics diagnostics = e.getDiagnostics();
                System.out.println("Fault injection triggered: " + diagnostics);
            }
        } finally {
            // Clean up
            serviceUnavailableRule.disable();
            System.clearProperty("COSMOS.THINCLIENT_ENABLED");
            gatewayV2AsyncClient.close();
        }
        // END: readme-sample-serviceUnavailableWithGatewayV2Scenario
    }
}
