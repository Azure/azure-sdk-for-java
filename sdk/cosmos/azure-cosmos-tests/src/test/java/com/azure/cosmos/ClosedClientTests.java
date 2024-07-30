// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.faultinjection.SessionRetryOptionsTests;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ClosedClientTransportException;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import org.assertj.core.api.AssertionsForClassTypes;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class ClosedClientTests extends TestSuiteBase {

    private static final Map<ConsistencyLevel, Integer> consistencyLevelToLinearizabilityRating = new HashMap<>();

    @Factory(dataProvider = "clientBuildersWithDirect")
    public ClosedClientTests(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fast"})
    public void beforeClass() {
        consistencyLevelToLinearizabilityRating.put(ConsistencyLevel.STRONG, 5);
        consistencyLevelToLinearizabilityRating.put(ConsistencyLevel.BOUNDED_STALENESS, 4);
        consistencyLevelToLinearizabilityRating.put(ConsistencyLevel.SESSION, 3);
        consistencyLevelToLinearizabilityRating.put(ConsistencyLevel.CONSISTENT_PREFIX, 2);
        consistencyLevelToLinearizabilityRating.put(ConsistencyLevel.EVENTUAL, 1);
    }

    @DataProvider(name = "operationProvider")
    public Object[] operationProvider() {
        return new Object[]{
            OperationType.Read,
            OperationType.Query,
            OperationType.Create,
            OperationType.Replace,
            OperationType.Delete,
            OperationType.Upsert,
            OperationType.Patch
        };
    }

    @Test(groups = {"fast"}, dataProvider = "operationProvider", timeOut = TIMEOUT)
    public void failFastWhenUsingClosedClient(OperationType operationType) {

        CosmosClientBuilder clientBuilder = getClientBuilder();

        ConnectionPolicy connectionPolicy = ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .getConnectionPolicy(clientBuilder);

        ConsistencyLevel desiredConsistencyLevel = ImplementationBridgeHelpers
            .CosmosClientBuilderHelper
            .getCosmosClientBuilderAccessor()
            .getConsistencyLevel(clientBuilder);

        if (connectionPolicy.getConnectionMode() != ConnectionMode.DIRECT) {
            throw new SkipException("Test only applicable in DIRECT connectivity mode!");
        }

        try {
            CosmosAsyncClient asyncClient = clientBuilder.buildAsyncClient();
            CosmosAsyncContainer cosmosAsyncContainer = getSharedSinglePartitionCosmosContainer(asyncClient);

            // Effective consistency level evaluates to account-level consistency
            // for write operations
            ConsistencyLevel accountLevelConsistency = ImplementationBridgeHelpers
                .CosmosAsyncClientHelper
                .getCosmosAsyncClientAccessor()
                .getEffectiveConsistencyLevel(asyncClient, OperationType.Create, null);

            if (!isAllowedConsistencyLevel(accountLevelConsistency, desiredConsistencyLevel)) {
                throw new SkipException(String.format("Client-level consistency : %s is stronger than account-level consistency %s, hence skipping the test!", desiredConsistencyLevel, accountLevelConsistency));
            }

            safeClose(asyncClient);

            performDocumentOperation(cosmosAsyncContainer, TestItem.createNewItem(), operationType);
            fail("Operation is expected to fail!");
        } catch (Exception ex) {

            if (ex instanceof SkipException) {
                throw ex;
            }

            CosmosException cosmosException = Utils.as(ex, CosmosException.class);

            assertThat(cosmosException).isNotNull();
            assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
            assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.INVALID_RESULT);
            assertThat(cosmosException.getCause() instanceof ClosedClientTransportException).isTrue();

            CosmosDiagnostics diagnostics = cosmosException.getDiagnostics();
            assertThat(diagnostics).isNotNull();

            CosmosDiagnosticsContext diagnosticsContext = diagnostics.getDiagnosticsContext();
            assertThat(diagnosticsContext).isNotNull();
            assertThat(diagnosticsContext.getRetryCount()).isLessThanOrEqualTo(1);
        }
    }

    private void performDocumentOperation(
        CosmosAsyncContainer asyncContainer,
        TestItem testItem,
        OperationType operationType) {

        if (operationType == OperationType.Query) {
            String query = String.format("SELECT * FROM c WHERE c.id = '%s'", testItem.getId());
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();

            SqlQuerySpec sqlQuerySpec = new SqlQuerySpec(query);

            asyncContainer
                .queryItems(sqlQuerySpec, queryRequestOptions, TestItem.class)
                .byPage()
                .blockFirst();
        }

        if (operationType == OperationType.Read) {

            asyncContainer
                .readItem(testItem.getId(), new PartitionKey(testItem.getId()), TestItem.class)
                .block();
        }

        if (operationType == OperationType.Create) {

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            asyncContainer
                .createItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();
        }

        if (operationType == OperationType.Replace) {

            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            asyncContainer
                .replaceItem(testItem, testItem.getId(), new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();
        }

        if (operationType == OperationType.Delete) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            asyncContainer
                .deleteItem(testItem.getId(), new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();
        }

        if (operationType == OperationType.Upsert) {
            CosmosItemRequestOptions itemRequestOptions = new CosmosItemRequestOptions();

            asyncContainer
                .upsertItem(testItem, new PartitionKey(testItem.getId()), itemRequestOptions)
                .block();
        }

        if (operationType == OperationType.Patch) {
            CosmosPatchOperations patchOperations = CosmosPatchOperations.create().add("/" + "newProperty", "newVal");

            asyncContainer
                .patchItem(testItem.getId(), new PartitionKey(testItem.getId()), patchOperations, TestItem.class)
                .block();
        }
    }

    private static boolean isAllowedConsistencyLevel(ConsistencyLevel accountLevelConsistency, ConsistencyLevel clientLevelConsistency) {
        return ClosedClientTests.consistencyLevelToLinearizabilityRating.get(accountLevelConsistency) >=
            ClosedClientTests.consistencyLevelToLinearizabilityRating.get(clientLevelConsistency);
    }
}
