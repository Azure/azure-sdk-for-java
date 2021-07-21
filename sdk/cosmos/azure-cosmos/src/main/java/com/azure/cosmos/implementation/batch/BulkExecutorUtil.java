// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosItemOperation;
import com.azure.cosmos.CosmosItemOperationType;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.TransactionalBatchOperationResult;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper.getEffectivePartitionKeyString;

final class BulkExecutorUtil {

    static ServerOperationBatchRequest createBatchRequest(List<CosmosItemOperation> operations, String partitionKeyRangeId) {

        return PartitionKeyRangeServerBatchRequest.createBatchRequest(
            partitionKeyRangeId,
            operations,
            BatchRequestResponseConstants.MAX_DIRECT_MODE_BATCH_REQUEST_BODY_SIZE_IN_BYTES,
            BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);
    }

    static void setRetryPolicyForBulk(
        AsyncDocumentClient docClientWrapper,
        CosmosAsyncContainer container,
        CosmosItemOperation cosmosItemOperation,
        ThrottlingRetryOptions throttlingRetryOptions) {

        if (cosmosItemOperation instanceof ItemBulkOperation<?>) {
            final ItemBulkOperation<?> itemBulkOperation = (ItemBulkOperation<?>) cosmosItemOperation;

            ResourceThrottleRetryPolicy resourceThrottleRetryPolicy = new ResourceThrottleRetryPolicy(
                throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
                throttlingRetryOptions.getMaxRetryWaitTime(),
                true);

            BulkOperationRetryPolicy bulkRetryPolicy = new BulkOperationRetryPolicy(
                docClientWrapper.getCollectionCache(),
                docClientWrapper.getPartitionKeyRangeCache(),
                BridgeInternal.getLink(container),
                resourceThrottleRetryPolicy);
            itemBulkOperation.setRetryPolicy(bulkRetryPolicy);

        } else {
            throw new UnsupportedOperationException("Unknown CosmosItemOperation.");
        }
    }

    static Map<String, String> getResponseHeadersFromBatchOperationResult(TransactionalBatchOperationResult result) {
        final Map<String, String> headers = new HashMap<>();

        headers.put(HttpConstants.HttpHeaders.SUB_STATUS, String.valueOf(result.getSubStatusCode()));
        headers.put(HttpConstants.HttpHeaders.E_TAG, result.getETag());
        headers.put(HttpConstants.HttpHeaders.REQUEST_CHARGE, String.valueOf(result.getRequestCharge()));

        if (result.getRetryAfterDuration() != null) {
            headers.put(HttpConstants.HttpHeaders.RETRY_AFTER_IN_MILLISECONDS, String.valueOf(result.getRetryAfterDuration().toMillis()));
        }

        return headers;
    }

    /**
     * Resolve partition key range id of a operation and set the partition key json value in operation.
     *
     * TODO(rakkuma): metaDataDiagnosticContext is passed null in tryLookupAsync function. Fix it while adding
     *  support for an operation wise Diagnostic. The value here should be merged in the individual diagnostic.
     * Issue: https://github.com/Azure/azure-sdk-for-java/issues/17647
     */
    static Mono<String> resolvePartitionKeyRangeId(
        AsyncDocumentClient docClientWrapper,
        CosmosAsyncContainer container,
        CosmosItemOperation operation) {

        checkNotNull(operation, "expected non-null operation");

        if (operation instanceof ItemBulkOperation<?>) {
            final ItemBulkOperation<?> itemBulkOperation = (ItemBulkOperation<?>) operation;

            final Mono<String> pkRangeIdMono = BulkExecutorUtil.getCollectionInfoAsync(docClientWrapper, container)
                .flatMap(collection -> {
                    final PartitionKeyDefinition definition = collection.getPartitionKey();
                    final PartitionKeyInternal partitionKeyInternal = getPartitionKeyInternal(operation, definition);
                    itemBulkOperation.setPartitionKeyJson(partitionKeyInternal.toJson());

                    return docClientWrapper.getPartitionKeyRangeCache()
                        .tryLookupAsync(null, collection.getResourceId(), null, null)
                        .map((Utils.ValueHolder<CollectionRoutingMap> routingMap) -> {

                            return routingMap.v.getRangeByEffectivePartitionKey(
                                getEffectivePartitionKeyString(
                                    partitionKeyInternal,
                                    definition)).getId();
                        });
                });

            return pkRangeIdMono;
        } else {
            throw new UnsupportedOperationException("Unknown CosmosItemOperation.");
        }
    }

    private static PartitionKeyInternal getPartitionKeyInternal(
        final CosmosItemOperation operation,
        final PartitionKeyDefinition partitionKeyDefinition) {

        checkNotNull(operation, "expected non-null operation");

        final PartitionKey partitionKey = operation.getPartitionKeyValue();
        if (partitionKey == null) {
            return ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
        } else {
            return BridgeInternal.getPartitionKeyInternal(partitionKey);
        }
    }

    /**
     * TODO(rakkuma): metaDataDiagnosticContext is passed null in resolveByNameAsync function. Fix it while adding
     *  support for an operation wise Diagnostic. The value here should be merged in the individual diagnostic.
     * Issue: https://github.com/Azure/azure-sdk-for-java/issues/17647
     */
    private static Mono<DocumentCollection> getCollectionInfoAsync(
        AsyncDocumentClient documentClient,
        CosmosAsyncContainer container) {

        // Utils.joinPath sanitizes the path and make sure it ends with a single '/'.
        final String resourceAddress = Utils.joinPath(BridgeInternal.getLink(container), null);

        final RxClientCollectionCache clientCollectionCache = documentClient.getCollectionCache();
        return clientCollectionCache
            .resolveByNameAsync(
                null,
                resourceAddress,
                null);
    }

    static boolean isWriteOperation(CosmosItemOperationType cosmosItemOperationType) {
        return cosmosItemOperationType == CosmosItemOperationType.CREATE ||
            cosmosItemOperationType == CosmosItemOperationType.REPLACE ||
            cosmosItemOperationType == CosmosItemOperationType.UPSERT ||
            cosmosItemOperationType == CosmosItemOperationType.DELETE ||
            cosmosItemOperationType == CosmosItemOperationType.PATCH;
    }
}
