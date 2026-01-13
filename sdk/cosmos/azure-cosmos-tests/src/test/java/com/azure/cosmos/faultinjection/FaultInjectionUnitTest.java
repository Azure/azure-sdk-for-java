// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.faultinjection;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionErrorType;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class FaultInjectionUnitTest {

    @Test(groups = "unit")
    public void testFaultInjectionBuilder() {
        String ruleId = "rule_id_1";
        FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
            .operationType(FaultInjectionOperationType.CREATE_ITEM)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();
        FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder(ruleId)
            .condition(faultInjectionCondition)
            .duration(Duration.ofSeconds(1))
            .result(FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                .delay(Duration.ofSeconds(6)) // default connection timeout is 5s
                .times(1)
                .build())
            .build();

        Assertions.assertThat(faultInjectionRule.getId()).isEqualTo(ruleId);
        Assertions.assertThat(faultInjectionRule.getCondition()).isEqualTo(faultInjectionCondition);
        Assertions.assertThat(faultInjectionRule.getDuration()).isEqualTo(Duration.ofSeconds(1));
        Assertions.assertThat(faultInjectionRule.getResult()).isNotNull();
    }

    @Test(groups = "unit")
    public void faultInjectionRule_metadataRequestConfig() {
        // validate for metadata request, only CONNECTION_DELAY, RESPONSE_DELAY, TOO_MANY_REQUEST error type supported
        List<FaultInjectionOperationType> metadataOperationTypes =
            Arrays.asList(
                FaultInjectionOperationType.METADATA_REQUEST_ADDRESS_REFRESH,
                FaultInjectionOperationType.METADATA_REQUEST_CONTAINER,
                FaultInjectionOperationType.METADATA_REQUEST_DATABASE_ACCOUNT,
                FaultInjectionOperationType.METADATA_REQUEST_QUERY_PLAN,
                FaultInjectionOperationType.METADATA_REQUEST_PARTITION_KEY_RANGES);

        List<FaultInjectionServerErrorType> validMetadataServerErrorTypes =
            Arrays.asList(
                FaultInjectionServerErrorType.TOO_MANY_REQUEST,
                FaultInjectionServerErrorType.CONNECTION_DELAY,
                FaultInjectionServerErrorType.RESPONSE_DELAY);


        for (FaultInjectionOperationType faultInjectionOperationTpe : FaultInjectionOperationType.values()) {
            for (FaultInjectionServerErrorType faultInjectionServerErrorType : FaultInjectionServerErrorType.values()) {

                if (metadataOperationTypes.contains(faultInjectionOperationTpe) && !validMetadataServerErrorTypes.contains(faultInjectionServerErrorType)) {
                    try {
                        new FaultInjectionRuleBuilder("metadataRule")
                            .condition(new FaultInjectionConditionBuilder().operationType(faultInjectionOperationTpe).build())
                            .result(
                                FaultInjectionResultBuilders
                                    .getResultBuilder(faultInjectionServerErrorType)
                                    .delay(Duration.ofSeconds(1))
                                    .build())
                            .build();

                        fail(String.format(
                                "faultInjectionRule should have failed to create. FaultInjectionOperationType %s, FaultInjectionServerErrorType %s",
                                faultInjectionOperationTpe,
                                faultInjectionServerErrorType));
                    } catch (IllegalArgumentException e) {
                        //no-op
                    }
                } else {
                    // Validate the rule can be created successfully
                    new FaultInjectionRuleBuilder("metadataRule")
                        .condition(new FaultInjectionConditionBuilder().operationType(faultInjectionOperationTpe).build())
                        .result(
                            FaultInjectionResultBuilders
                                .getResultBuilder(faultInjectionServerErrorType)
                                .delay(Duration.ofSeconds(1))
                                .build())
                        .build();
                }
            }
        }
    }

    @Test(groups = "unit")
    public void faultInjectionRule_gatewayConnectionConfig() {
        // Validate no connection error type can be configured
        try {
            new FaultInjectionRuleBuilder("gatewayFaultInjectionRule")
                .condition(new FaultInjectionConditionBuilder().connectionType(FaultInjectionConnectionType.GATEWAY).build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionConnectionErrorType.CONNECTION_CLOSE)
                        .interval(Duration.ofSeconds(1))
                        .build())
                .build();

            fail("gatewayFaultInjection rule should have failed as no connection error is supported for gateway connection type.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("FaultInjectionConnectionError result can not be configured for rule with gateway connection type."));
        }

        //validate no GONE exception can be configured on gateway connection
        try {
            new FaultInjectionRuleBuilder("gatewayFaultInjectionRule")
                .condition(new FaultInjectionConditionBuilder().connectionType(FaultInjectionConnectionType.GATEWAY).build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.GONE)
                        .build())
                .build();

            fail("gatewayFaultInjection rule should have failed as GONE error is not supported for gateway connection type.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Gone exception can not be injected for rule with gateway connection type"));
        }

        // validate STALED_ADDRESSES_SERVER_GONE error can not be injected on gateway connection
        try {
            new FaultInjectionRuleBuilder("gatewayFaultInjectionRule")
                .condition(
                    new FaultInjectionConditionBuilder()
                        .connectionType(FaultInjectionConnectionType.GATEWAY)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.STALED_ADDRESSES_SERVER_GONE)
                        .build())
                .build();

            fail("gatewayFaultInjection rule should have failed as STALED_ADDRESSES_SERVER_GONE error is not supported for gateway connection type.");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("STALED_ADDRESSES exception can not be injected for rule with gateway connection type"));
        }
    }

    @Test
    public void transactionalBatch() {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient();

        TestObject testItem = TestObject.create();

        CosmosAsyncContainer container = client.getDatabase("testdb").getContainer("testcontainer");
        CosmosBatch cosmosBatch = CosmosBatch.createCosmosBatch(new PartitionKey(testItem.getId()));
        cosmosBatch.createItemOperation(testItem);
        cosmosBatch.deleteItemOperation(testItem.getId());
        cosmosBatch.replaceItemOperation(testItem.getId(), testItem);

        container.executeCosmosBatch(cosmosBatch)
            .flatMapMany(response -> {
                System.out.println("coming to normal response: " + response.getStatusCode());
                return Flux.fromIterable(response.getResults());
            })
            .flatMap(operationResult -> {
                System.out.println("opreation result: " + operationResult.getOperation().getOperationType() + ":" + operationResult.getStatusCode());
                return Mono.empty();
            })
            .blockLast();
    }

    @Test
    public void changeFeedTests() {
        CosmosAsyncClient client = new CosmosClientBuilder()
            .key(TestConfigurations.MASTER_KEY)
            .endpoint(TestConfigurations.HOST)
            .buildAsyncClient();

        CosmosAsyncContainer container = client.getDatabase("testdb").getContainer("testcontainer2");
        CosmosChangeFeedRequestOptions changeFeedRequestOptions =
            CosmosChangeFeedRequestOptions.createForProcessingFromPointInTime(Instant.ofEpochMilli(1767484800000l), FeedRange.forFullRange());
        container.queryChangeFeed(changeFeedRequestOptions, JsonNode.class)
            .byPage()
            .flatMap(response -> {
                System.out.println(response.getContinuationToken());
                return Mono.empty();
            })
            .blockLast();
    }
}
