// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.FaultInjectionBridgeInternal;
import com.azure.cosmos.FaultInjectionConditionBuilder;
import com.azure.cosmos.FaultInjectionResultBuilders;
import com.azure.cosmos.FaultInjectionRuleBuilder;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.RxDocumentClientImpl;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FaultInjectionEndpoints;
import com.azure.cosmos.models.FaultInjectionOperationType;
import com.azure.cosmos.models.FaultInjectionRule;
import com.azure.cosmos.models.FaultInjectionServerErrorType;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FaultInjectionServerErrorRuleTests extends TestSuiteBase {

    private static final int TIMEOUT = 60000;
    private CosmosAsyncClient client;
    private CosmosAsyncContainer cosmosAsyncContainer;
    private DatabaseAccount databaseAccount;

    @BeforeClass(groups = {"multi-region"},timeOut = TIMEOUT)
    public void beforeClass() {
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .contentResponseOnWriteEnabled(true)
            .directMode()
            .buildAsyncClient();
        AsyncDocumentClient asyncDocumentClient = ReflectionUtils.getAsyncDocumentClient(client);
        RxDocumentClientImpl rxDocumentClient = (RxDocumentClientImpl) asyncDocumentClient;
        GlobalEndpointManager globalEndpointManager =
            ReflectionUtils.getGlobalEndpointManager(rxDocumentClient);
        DatabaseAccount databaseAccount = globalEndpointManager.getLatestDatabaseAccount();
        this.databaseAccount = databaseAccount;
        this.cosmosAsyncContainer = getSharedMultiPartitionCosmosContainerWithIdAsPartitionKey(client);
    }

    @DataProvider
    public static Object[][] operationTypeProvider() {
        return new Object[][]{
            { OperationType.Read },
            { OperationType.Replace },
            { OperationType.Create },
            { OperationType.Delete },
            { OperationType.Query },
        };
    }

    @Test(groups = {"multi-region"}, dataProvider = "operationTypeProvider", timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_OperationType(OperationType operationType) throws JsonProcessingException {
        // create a new item to be used by read operations
        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        String ruleId = "serverErrorRule-OperationType-" + UUID.randomUUID();
        // with matching operationType
        FaultInjectionRule serverErrorRule =
            new FaultInjectionRuleBuilder(ruleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ)
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.SERVER_GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionBridgeInternal.configFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverErrorRule)).block();
        List<URI> physicalAddresses = serverErrorRule.getAddresses();
        assertThat(physicalAddresses.size()).isZero();

        CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(cosmosAsyncContainer, operationType, createdItem);

        List<ObjectNode> diagnosticsNode = new ArrayList<>();
        if (operationType == OperationType.Query) {
            int clientSideDiagnosticsIndex = cosmosDiagnostics.toString().indexOf("[{\"userAgent\"");
            ArrayNode arrayNode = (ArrayNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString().substring(clientSideDiagnosticsIndex));
            for (JsonNode node : arrayNode) {
                diagnosticsNode.add((ObjectNode) node);
            }
        } else {
            diagnosticsNode.add((ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString()));
        }

        for (ObjectNode diagnosticNode : diagnosticsNode) {
            if (operationType == OperationType.Read) {
                this.validateFaultInjectionApplied(
                    diagnosticNode,
                    HttpConstants.StatusCodes.GONE,
                    HttpConstants.SubStatusCodes.FAULT_INJECTION_ERROR,
                    ruleId);
            } else {
                JsonNode responseStatisticsList = diagnosticNode.get("responseStatisticsList");
                Assertions.assertThat(responseStatisticsList.size()).isOne();
            }
        }

        serverErrorRule.disable();
    }

    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Region() throws JsonProcessingException {
        Iterator<DatabaseAccountLocation> locationIterator = this.databaseAccount.getReadableLocations().iterator();
        List<String> preferredLocations = new ArrayList<>();

        while (locationIterator.hasNext()) {
            DatabaseAccountLocation accountLocation = locationIterator.next();
            preferredLocations.add(accountLocation.getName());
        }

        CosmosAsyncClient clientWithPreferredRegion = null;
        try {
            clientWithPreferredRegion = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .preferredRegions(preferredLocations)
                .directMode()
                .buildAsyncClient();

            CosmosAsyncContainer container =
                clientWithPreferredRegion
                    .getDatabase(this.cosmosAsyncContainer.getDatabase().getId())
                    .getContainer(this.cosmosAsyncContainer.getId());

            TestItem createdItem = TestItem.createNewItem();
            container.createItem(createdItem).block();

            // set local region rule
            String localRegionRuleId = "ServerErrorRule-LocalRegion-" + UUID.randomUUID();
            FaultInjectionRule serverErrorRuleLocalRegion =
                new FaultInjectionRuleBuilder(localRegionRuleId)
                    .condition(
                        new FaultInjectionConditionBuilder()
                            .region(preferredLocations.get(0))
                            .build()
                    )
                    .result(
                        FaultInjectionResultBuilders
                            .getResultBuilder(FaultInjectionServerErrorType.SERVER_GONE)
                            .times(1)
                            .build()
                    )
                    .duration(Duration.ofMinutes(5))
                    .build();

            // set remote region rule
            String remoteRegionRuleId = "ServerErrorRule-RemoteRegion-" + UUID.randomUUID();
            FaultInjectionRule serverErrorRuleLocalRemoteRegion =
                new FaultInjectionRuleBuilder(remoteRegionRuleId)
                    .condition(
                        new FaultInjectionConditionBuilder()
                            .region(preferredLocations.get(1))
                            .build()
                    )
                    .result(
                        FaultInjectionResultBuilders
                            .getResultBuilder(FaultInjectionServerErrorType.SERVER_GONE)
                            .times(1)
                            .build()
                    )
                    .duration(Duration.ofMinutes(5))
                    .build();

            FaultInjectionBridgeInternal
                .configFaultInjectionRules(container, Arrays.asList(serverErrorRuleLocalRegion, serverErrorRuleLocalRemoteRegion))
                .block();

            // Validate fault injection applied in the local region
            CosmosDiagnostics cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem);
            ObjectNode diagnosticNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
            this.validateFaultInjectionApplied(
                diagnosticNode,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.FAULT_INJECTION_ERROR,
                localRegionRuleId);

            // now disable the local region ruleId, validate no fault injection rule is applied
            serverErrorRuleLocalRegion.disable();
            cosmosDiagnostics = this.performDocumentOperation(container, OperationType.Read, createdItem);
            diagnosticNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString());
            JsonNode responseStatisticsList = diagnosticNode.get("responseStatisticsList");
            Assertions.assertThat(responseStatisticsList.size()).isOne();

            serverErrorRuleLocalRemoteRegion.disable();
        } finally {
            safeClose(clientWithPreferredRegion);
        }
    }


    @Test(groups = {"multi-region"}, timeOut = TIMEOUT)
    public void faultInjectionServerErrorRuleTests_Partition() throws JsonProcessingException {

        TestItem createdItem = TestItem.createNewItem();
        cosmosAsyncContainer.createItem(createdItem).block();

        List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();

        // set rule by feed range
        String feedRangeRuleId = "ServerErrorRule-FeedRange-" + UUID.randomUUID();

        FaultInjectionRule serverErrorRuleByFeedRange =
            new FaultInjectionRuleBuilder(feedRangeRuleId)
                .condition(
                    new FaultInjectionConditionBuilder()
                        .endpoints(new FaultInjectionEndpoints(feedRanges.get(0))) // by default setting on all replicas
                        .build()
                )
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.SERVER_GONE)
                        .times(1)
                        .build()
                )
                .duration(Duration.ofMinutes(5))
                .build();

        FaultInjectionBridgeInternal.configFaultInjectionRules(cosmosAsyncContainer, Arrays.asList(serverErrorRuleByFeedRange)).block();

        // Issue a query to the feed range which configured fault injection rule and validate fault injection rule is applied
        String query = "select * from c";
        CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
        queryRequestOptions.setFeedRange(feedRanges.get(0));

        CosmosDiagnostics cosmosDiagnostics =
            cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst().getCosmosDiagnostics();
        int clientSideDiagnosticsIndex = cosmosDiagnostics.toString().indexOf("[{\"userAgent\"");
        ArrayNode arrayNode = (ArrayNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString().substring(clientSideDiagnosticsIndex));
        for (JsonNode node : arrayNode) {
            this.validateFaultInjectionApplied(
                (ObjectNode) node,
                HttpConstants.StatusCodes.GONE,
                HttpConstants.SubStatusCodes.FAULT_INJECTION_ERROR,
                feedRangeRuleId);
        }

        // Issue a query to the feed range which is not configured fault injection rule and validate no fault injection is applied
        queryRequestOptions.setFeedRange(feedRanges.get(1));
        cosmosDiagnostics = cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst().getCosmosDiagnostics();
        clientSideDiagnosticsIndex = cosmosDiagnostics.toString().indexOf("[{\"userAgent\"");
        arrayNode = (ArrayNode) Utils.getSimpleObjectMapper().readTree(cosmosDiagnostics.toString().substring(clientSideDiagnosticsIndex));
        for (JsonNode node : arrayNode) {
            JsonNode responseStatisticsList = node.get("responseStatisticsList");
            Assertions.assertThat(responseStatisticsList.isArray()).isTrue();
            Assertions.assertThat(responseStatisticsList.size()).isOne();
        }

        serverErrorRuleByFeedRange.disable();
    }

    @AfterClass(groups = {"multi-region"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeClose(client);
    }

    private CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestItem createdItem) {
        try {
            if (operationType == OperationType.Query) {
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
                FeedResponse<TestItem> itemFeedResponse =
                    cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestItem.class).byPage().blockFirst();

                return itemFeedResponse.getCosmosDiagnostics();
            }

            if (operationType == OperationType.Read
                || operationType == OperationType.Delete
                || operationType == OperationType.Replace
                || operationType == OperationType.Create) {

                if (operationType == OperationType.Read) {
                    return cosmosAsyncContainer.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk()),
                        TestItem.class).block().getDiagnostics();
                }

                if (operationType == OperationType.Replace) {
                    return cosmosAsyncContainer.replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getMypk())).block().getDiagnostics();
                }

                if (operationType == OperationType.Delete) {
                    return cosmosAsyncContainer.deleteItem(createdItem, null).block().getDiagnostics();
                }

                if (operationType == OperationType.Create) {
                    return cosmosAsyncContainer.createItem(TestItem.createNewItem()).block().getDiagnostics();
                }
            }

            throw new IllegalArgumentException("The operation type is not supported");
        } catch (CosmosException cosmosException) {
            return cosmosException.getDiagnostics();
        }
    }

    private void validateFaultInjectionApplied(
        ObjectNode diagnosticNode,
        int statusCode,
        int subStatusCode,
        String ruleId) {

        JsonNode responseStatisticsList = diagnosticNode.get("responseStatisticsList");
        Assertions.assertThat(responseStatisticsList.isArray()).isTrue();

        Assertions.assertThat(responseStatisticsList.size()).isEqualTo(2);
        JsonNode storeResult = responseStatisticsList.get(0).get("storeResult");
        Assertions.assertThat(storeResult).isNotNull();
        assertThat(storeResult.get("statusCode").asInt()).isEqualTo(statusCode);
        assertThat(storeResult.get("subStatusCode").asInt()).isEqualTo(subStatusCode);
        assertThat(storeResult.get("exceptionMessage").asText()).contains("ruleId [" + ruleId + "]");
    }
}
