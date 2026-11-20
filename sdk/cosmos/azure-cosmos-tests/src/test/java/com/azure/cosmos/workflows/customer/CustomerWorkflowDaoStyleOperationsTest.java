// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBatchResponse;
import com.azure.cosmos.models.CosmosBulkExecutionOptions;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowDaoStyleOperationsTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowDaoStyleOperationsTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer DAO-style workflow tests", true);
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = TIMEOUT)
    public void crudReadAllPatchBatchAndBulkWorkflow() {
        List<String> excludedRegions = excludeFirstWritableRegion();
        TestObject item = TestObject.create();

        CosmosItemRequestOptions createOptions = new CosmosItemRequestOptions()
            .setKeywordIdentifiers(Collections.singleton("workflow-crud-create"))
            .setExcludedRegions(excludedRegions)
            .setCustomItemSerializer(CosmosItemSerializer.DEFAULT_SERIALIZER)
            .setContentResponseOnWriteEnabled(true);

        CosmosItemResponse<TestObject> createResponse = this.container
            .createItem(item, createOptions)
            .block();

        assertThat(createResponse).isNotNull();
        registerForCleanup(item);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.CREATED);
        assertKeywordIdentifier(createResponse.getDiagnostics().getDiagnosticsContext(), "workflow-crud-create");
        assertThat(getRequestOptions(createResponse.getDiagnostics().getDiagnosticsContext()).getCustomItemSerializer())
            .isSameAs(CosmosItemSerializer.DEFAULT_SERIALIZER);
        assertDidNotContactExcludedRegions(createResponse.getDiagnostics().getDiagnosticsContext(), excludedRegions);

        CosmosItemResponse<TestObject> readResponse = this.container
            .readItem(item.getId(), partitionKey(item), new CosmosItemRequestOptions().setExcludedRegions(excludedRegions), TestObject.class)
            .block();

        assertThat(readResponse).isNotNull();
        assertThat(readResponse.getItem()).isEqualTo(item);

        FeedResponse<TestObject> readAllResponse = this.container
            .readAllItems(
                partitionKey(item),
                new CosmosQueryRequestOptions()
                    .setExcludedRegions(excludedRegions)
                    .setCustomItemSerializer(CosmosItemSerializer.DEFAULT_SERIALIZER),
                TestObject.class)
            .byPage()
            .blockFirst();

        assertThat(readAllResponse).isNotNull();
        assertThat(readAllResponse.getResults()).extracting(TestObject::getId).contains(item.getId());
        assertExcludedRegions(readAllResponse.getCosmosDiagnostics().getDiagnosticsContext(), excludedRegions);
        assertThat(getRequestOptions(readAllResponse.getCosmosDiagnostics().getDiagnosticsContext()).getCustomItemSerializer())
            .isSameAs(CosmosItemSerializer.DEFAULT_SERIALIZER);

        CosmosPatchOperations patchOperations = CosmosPatchOperations.create()
            .set("/stringProp", "patched-" + item.getStringProp());

        CosmosItemResponse<TestObject> patchResponse = this.container
            .patchItem(item.getId(), partitionKey(item), patchOperations, TestObject.class)
            .block();

        assertThat(patchResponse).isNotNull();
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.OK);
        assertThat(patchResponse.getItem().getStringProp()).startsWith("patched-");

        String batchPk = "batch-" + UUID.randomUUID();
        TestObject batchItem = TestObject.create(batchPk);
        CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey(batchItem));
        batch.createItemOperation(batchItem);
        batch.readItemOperation(batchItem.getId());

        CosmosBatchResponse batchResponse = this.container.executeCosmosBatch(batch).block();

        assertThat(batchResponse).isNotNull();
        registerForCleanup(batchItem);
        assertThat(batchResponse.isSuccessStatusCode()).isTrue();
        assertThat(batchResponse.size()).isEqualTo(2);
        assertThat(batchResponse.getDiagnostics()).isNotNull();

        TestObject bulkItem = TestObject.create();
        this.container.createItem(bulkItem).block();
        registerForCleanup(bulkItem);
        CosmosPatchOperations bulkPatchOperations = CosmosPatchOperations.create()
            .set("/stringProp", "bulk-patched-" + bulkItem.getStringProp());

        List<CosmosItemOperation> bulkOperations = new ArrayList<>();
        bulkOperations.add(CosmosBulkOperations.getReadItemOperation(bulkItem.getId(), partitionKey(bulkItem)));
        bulkOperations.add(CosmosBulkOperations.getPatchItemOperation(bulkItem.getId(), partitionKey(bulkItem), bulkPatchOperations));

        CosmosBulkExecutionOptions bulkExecutionOptions = new CosmosBulkExecutionOptions()
            .setMaxMicroBatchSize(2)
            .setExcludedRegions(excludedRegions)
            .setKeywordIdentifiers(Collections.singleton("workflow-bulk"));

        List<CosmosBulkOperationResponse<Object>> bulkResponses = this.container
            .executeBulkOperations(Flux.fromIterable(bulkOperations), bulkExecutionOptions)
            .collectList()
            .block();

        assertThat(bulkResponses).isNotNull();
        assertThat(bulkResponses).hasSize(2);
        assertThat(bulkResponses).allSatisfy(response -> {
            assertThat(response.getException()).isNull();
            assertThat(response.getResponse().getStatusCode()).isIn(HttpConstants.StatusCodes.OK, HttpConstants.StatusCodes.CREATED);
            assertThat(response.getResponse().getCosmosDiagnostics()).isNotNull();
        });
    }
}
