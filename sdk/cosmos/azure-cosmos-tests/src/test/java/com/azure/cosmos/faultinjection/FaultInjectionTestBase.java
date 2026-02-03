// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.models.CosmosBatch;
import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosItemIdentity;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class FaultInjectionTestBase extends TestSuiteBase {
    public FaultInjectionTestBase(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    protected CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestObject createdItem,
        boolean isReadMany) {

        return performDocumentOperation(
            cosmosAsyncContainer,
            operationType,
            createdItem,
            isReadMany,
            true,
            false);
    }

    protected CosmosDiagnostics performDocumentOperation(
        CosmosAsyncContainer cosmosAsyncContainer,
        OperationType operationType,
        TestObject createdItem,
        boolean isReadMany,
        boolean fetchFeedRangesBeforehandForChangeFeed,
        boolean isBulkOperation) {

        try {
            if (operationType == OperationType.Query && !isReadMany) {
                CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
                String query = String.format("SELECT * from c where c.id = '%s'", createdItem.getId());
                FeedResponse<TestObject> itemFeedResponse =
                    cosmosAsyncContainer.queryItems(query, queryRequestOptions, TestObject.class).byPage().blockLast();

                return itemFeedResponse.getCosmosDiagnostics();
            }

            if (operationType == OperationType.Read
                || operationType == OperationType.Delete
                || operationType == OperationType.Replace
                || operationType == OperationType.Create
                || operationType == OperationType.Patch
                || operationType == OperationType.Upsert
                || operationType == OperationType.Batch) {

                if (operationType == OperationType.Read) {
                    return cosmosAsyncContainer.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId()),
                        TestObject.class).block().getDiagnostics();
                }

                if (operationType == OperationType.Replace) {
                    return cosmosAsyncContainer.replaceItem(
                        createdItem,
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId())).block().getDiagnostics();
                }

                if (operationType == OperationType.Delete) {
                    return cosmosAsyncContainer.deleteItem(createdItem, null).block().getDiagnostics();
                }

                if (operationType == OperationType.Create) {
                    return cosmosAsyncContainer.createItem(TestObject.create()).block().getDiagnostics();
                }

                if (operationType == OperationType.Upsert) {
                    return cosmosAsyncContainer.upsertItem(TestObject.create()).block().getDiagnostics();
                }

                if (operationType == OperationType.Patch) {
                    CosmosPatchOperations patchOperations =
                        CosmosPatchOperations
                            .create()
                            .add("/newPath", "newPath");
                    return cosmosAsyncContainer
                        .patchItem(createdItem.getId(), new PartitionKey(createdItem.getId()), patchOperations, TestObject.class)
                        .block().getDiagnostics();
                }

                if (operationType == OperationType.Batch) {

                    if (isBulkOperation) {

                        List<CosmosItemOperation> cosmosItemOperations = new ArrayList<>();

                        CosmosItemOperation cosmosItemOperation = CosmosBulkOperations.getReadItemOperation(
                            createdItem.getId(),
                            new PartitionKey(createdItem.getId()),
                            TestObject.class);

                        cosmosItemOperations.add(cosmosItemOperation);

                        Flux<CosmosItemOperation> operationsFlux = Flux.fromIterable(cosmosItemOperations);

                        CosmosBulkOperationResponse<Object> response = cosmosAsyncContainer.executeBulkOperations(operationsFlux).blockLast();

                        assertThat(response).isNotNull();

                        return response.getResponse().getCosmosDiagnostics();
                    } else {
                        CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(createdItem.getId()));

                        batch.upsertItemOperation(createdItem);
                        batch.readItemOperation(createdItem.getId());

                        return cosmosAsyncContainer.executeCosmosBatch(batch).block().getDiagnostics();
                    }
                }
            }

            if (operationType == OperationType.ReadFeed) {

                if (fetchFeedRangesBeforehandForChangeFeed) {
                    List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();

                    assertThat(feedRanges).isNotNull();
                    assertThat(feedRanges.size()).isGreaterThan(0);

                    CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                        CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRanges.get(0));

                    FeedResponse<TestObject> firstPage =  cosmosAsyncContainer
                        .queryChangeFeed(changeFeedRequestOptions, TestObject.class)
                        .byPage()
                        .blockFirst();
                    return firstPage.getCosmosDiagnostics();
                }

                CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                    CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(FeedRange.forFullRange());

                FeedResponse<TestObject> firstPage =  cosmosAsyncContainer
                    .queryChangeFeed(changeFeedRequestOptions, TestObject.class)
                    .byPage()
                    .blockFirst();
                return firstPage.getCosmosDiagnostics();
            }

            if (operationType == OperationType.Query) {
                return cosmosAsyncContainer.readMany(
                    Arrays.asList(new CosmosItemIdentity(new PartitionKey(createdItem.getId()), createdItem.getId()), new CosmosItemIdentity(new PartitionKey(createdItem.getId()), createdItem.getId())),
                    TestObject.class).block().getCosmosDiagnostics();
            }

            throw new IllegalArgumentException("The operation type is not supported");
        } catch (CosmosException cosmosException) {
            return cosmosException.getDiagnostics();
        }
    }
}
