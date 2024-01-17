// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.throughputControl.TestItem;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.rx.TestSuiteBase;

import java.util.List;

public abstract class FaultInjectionTestBase extends TestSuiteBase {
    public FaultInjectionTestBase(CosmosClientBuilder cosmosClientBuilder) {
        super(cosmosClientBuilder);
    }

    protected CosmosDiagnostics performDocumentOperation(
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
                || operationType == OperationType.Create
                || operationType == OperationType.Patch
                || operationType == OperationType.Upsert) {

                if (operationType == OperationType.Read) {
                    return cosmosAsyncContainer.readItem(
                        createdItem.getId(),
                        new PartitionKey(createdItem.getId()),
                        TestItem.class).block().getDiagnostics();
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
                    return cosmosAsyncContainer.createItem(TestItem.createNewItem()).block().getDiagnostics();
                }

                if (operationType == OperationType.Upsert) {
                    return cosmosAsyncContainer.upsertItem(TestItem.createNewItem()).block().getDiagnostics();
                }

                if (operationType == OperationType.Patch) {
                    CosmosPatchOperations patchOperations =
                        CosmosPatchOperations
                            .create()
                            .add("newPath", "newPath");
                    return cosmosAsyncContainer
                        .patchItem(createdItem.getId(), new PartitionKey(createdItem.getId()), patchOperations, TestItem.class)
                        .block().getDiagnostics();
                }
            }

            if (operationType == OperationType.ReadFeed) {
                List<FeedRange> feedRanges = cosmosAsyncContainer.getFeedRanges().block();
                CosmosChangeFeedRequestOptions changeFeedRequestOptions =
                    CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(feedRanges.get(0));

                FeedResponse<TestItem> firstPage =  cosmosAsyncContainer
                    .queryChangeFeed(changeFeedRequestOptions, TestItem.class)
                    .byPage()
                    .blockFirst();
                return firstPage.getCosmosDiagnostics();
            }

            throw new IllegalArgumentException("The operation type is not supported");
        } catch (CosmosException cosmosException) {
            return cosmosException.getDiagnostics();
        }
    }
}
