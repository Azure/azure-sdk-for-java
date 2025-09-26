// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.CollectionRoutingMapNotFoundException;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.CosmosBatchOperationResult;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosItemOperationType;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper.getEffectivePartitionKeyString;

final class BulkExecutorUtil {

    static ServerOperationBatchRequest createBatchRequest(
        List<CosmosItemOperation> operations,
        String partitionKeyRangeId,
        int maxMicroBatchPayloadSizeInBytes,
        CosmosItemSerializer clientItemSerializer) {

        return PartitionKeyRangeServerBatchRequest.createBatchRequest(
            partitionKeyRangeId,
            operations,
            maxMicroBatchPayloadSizeInBytes,
            Math.min(operations.size(), BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST),
            clientItemSerializer);
    }

    static void setRetryPolicyForBulk(
        AsyncDocumentClient docClientWrapper,
        CosmosAsyncContainer container,
        CosmosItemOperation cosmosItemOperation,
        ThrottlingRetryOptions throttlingRetryOptions) {

        if (cosmosItemOperation instanceof ItemBulkOperation<?, ?>) {
            final ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) cosmosItemOperation;

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

    static Map<String, String> getResponseHeadersFromBatchOperationResult(CosmosBatchOperationResult result) {
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

        AtomicReference<DocumentCollection> collectionBeforeRecreation = new AtomicReference<>(null);

        if (operation instanceof ItemBulkOperation<?, ?>) {
            final ItemBulkOperation<?, ?> itemBulkOperation = (ItemBulkOperation<?, ?>) operation;

            return Mono.defer(() ->
                BulkExecutorUtil.getCollectionInfoAsync(docClientWrapper, container, collectionBeforeRecreation.get())
                .flatMap(collection -> {
                    final PartitionKeyDefinition definition = collection.getPartitionKey();
                    final PartitionKeyInternal partitionKeyInternal = getPartitionKeyInternal(operation, definition);
                    itemBulkOperation.setPartitionKeyJson(partitionKeyInternal.toJson());

                    StringBuilder sb = new StringBuilder();
                    sb.append("BulkExecutorUtil.resolvePartitionKeyRangeId:").append(",");

                    return docClientWrapper.getPartitionKeyRangeCache()
                        .tryLookupAsync(null, collection.getResourceId(), null, null, sb)
                        .map((Utils.ValueHolder<CollectionRoutingMap> routingMap) -> {

                            if (routingMap.v == null) {
                                collectionBeforeRecreation.set(collection);
                                throw new CollectionRoutingMapNotFoundException(
                                    String.format(
                                        "No collection routing map found for container %s(%s) in database %s.",
                                        container.getId(),
                                        collection.getResourceId(),
                                        container.getDatabase().getId())
                                        );
                            }

                            return routingMap.v.getRangeByEffectivePartitionKey(
                                getEffectivePartitionKeyString(
                                    partitionKeyInternal,
                                    definition)).getId();
                        });
                }))
                .retryWhen(Retry
                    .fixedDelay(
                        BatchRequestResponseConstants.MAX_COLLECTION_RECREATION_RETRY_COUNT,
                        Duration.ofSeconds(
                            BatchRequestResponseConstants.MAX_COLLECTION_RECREATION_REFRESH_INTERVAL_IN_SECONDS))
                    .filter(t -> t instanceof CollectionRoutingMapNotFoundException)
                    .doBeforeRetry((retrySignal) -> docClientWrapper
                        .getCollectionCache()
                        .refresh(
                            null,
                            Utils.getCollectionName(BridgeInternal.getLink(container)),
                            null)
                    )
                );
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
        CosmosAsyncContainer container,
        DocumentCollection obsoleteValue) {

        // Utils.joinPath sanitizes the path and make sure it ends with a single '/'.
        final String resourceAddress = Utils.joinPath(BridgeInternal.getLink(container), null);

        final RxClientCollectionCache clientCollectionCache = documentClient.getCollectionCache();
        return clientCollectionCache
            .resolveByNameAsync(
                null,
                resourceAddress,
                null,
                obsoleteValue);
    }

    static boolean isWriteOperation(CosmosItemOperationType cosmosItemOperationType) {
        return cosmosItemOperationType == CosmosItemOperationType.CREATE ||
            cosmosItemOperationType == CosmosItemOperationType.REPLACE ||
            cosmosItemOperationType == CosmosItemOperationType.UPSERT ||
            cosmosItemOperationType == CosmosItemOperationType.DELETE ||
            cosmosItemOperationType == CosmosItemOperationType.PATCH;
    }
}
