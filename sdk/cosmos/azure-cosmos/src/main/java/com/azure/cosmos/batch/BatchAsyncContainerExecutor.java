// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.HttpConstants.HttpHeaders;
import com.azure.cosmos.implementation.directconnectivity.WFConstants.BackendHeaders;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import io.netty.util.HashedWheelTimer;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.*;
import static com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper.getEffectivePartitionKeyString;

/**
 * Bulk batch executor for operations in the same container.
 * <p>
 * It maintains one {@link BatchAsyncStreamer} for each Partition Key Range, which allows independent execution of
 * requests. Semaphores are in place to rate limit the operations at the Streamer / Partition Key Range level, this
 * means that we can send parallel and independent requests to different Partition Key Ranges, but for the same Range,
 * requests will be limited. Two delegate implementations define how a particular request should be executed, and how
 * operations should be retried. When the {@link BatchAsyncStreamer} dispatches a batch, the batch will create a request
 * and call the executeAsync delegate, if conditions are met, it might call the retry delegate.
 * <p>
 * {@link BatchAsyncStreamer}
 */
public class BatchAsyncContainerExecutor implements AutoCloseable {

    private static final int DEFAULT_MAX_DEGREE_OF_CONCURRENCY = 50;
    private final HashedWheelTimer timer = new HashedWheelTimer();
    private final ConcurrentHashMap<String, Semaphore> limiters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, BatchAsyncStreamer> streamers = new ConcurrentHashMap<>();

    private final CosmosAsyncContainer container;
    private final int maxOperationCount;
    private final int maxPayloadLength;
    private final ThrottlingRetryOptions throttlingRetryOptions;
    private final AsyncDocumentClient docClientWrapper;

    public BatchAsyncContainerExecutor(
        final CosmosAsyncContainer container,
        final int maxOperationCount,
        final int maxPayloadLength) {

        checkNotNull(container, "expected non-null containerCore");
        checkArgument(maxOperationCount > 0, "expected maxServerRequestOperationCount > 0, not %s", maxOperationCount);
        checkArgument(maxPayloadLength > 0, "expected maxServerRequestBodyLength > 0, not %s", maxPayloadLength);

        this.container = container;
        this.maxOperationCount = maxOperationCount;
        this.maxPayloadLength = maxPayloadLength;
        this.docClientWrapper = CosmosBridgeInternal.getAsyncDocumentClient(container.getDatabase());
        this.throttlingRetryOptions = docClientWrapper.getConnectionPolicy().getThrottlingRetryOptions();
    }

    public Mono<TransactionalBatchOperationResult<?>> addAsync(
        final ItemBatchOperation<?> operation,
        final RequestOptions options) {

        checkNotNull(operation, "expected non-null operation");

        CompletableFuture<TransactionalBatchOperationResult<?>> future = this.validateAndMaterializeOperation(operation, options)
            .thenComposeAsync(
                t -> this.resolvePartitionKeyRangeIdAsync(operation)
                    .thenCompose(
                        (String resolvedPartitionKeyRangeId) -> {

                            BatchAsyncStreamer streamer = this.getOrAddStreamerForPartitionKeyRange(resolvedPartitionKeyRangeId);

                            ItemBatchOperationContext context = new ItemBatchOperationContext(
                                resolvedPartitionKeyRangeId,
                                BatchAsyncContainerExecutor.getRetryPolicy(this.throttlingRetryOptions));
                            operation.attachContext(context);

                            streamer.add(operation);

                            return context.getOperationResultFuture();
                        }));

        return Mono.fromFuture(future);
    }

    public CompletableFuture<Boolean> validateAndMaterializeOperation(
        final ItemBatchOperation<?> operation,
        final RequestOptions options) {

        if (options != null) {
            checkState(options.getConsistencyLevel() == null
                && options.getPostTriggerInclude() == null
                && options.getPreTriggerInclude() == null
                && options.getSessionToken() == null, "UnsupportedBulkRequestOptions");

            BatchAsyncContainerExecutor.validateOperationEpk(operation, options);
        }

        return operation.materializeResource();
    }

    private Mono<PartitionKeyRangeBatchExecutionResult> executeAsync(PartitionKeyRangeServerBatchRequest request) throws Exception {
        request.setAtomicBatch(false);
        request.setShouldContinueOnError(true);

        final Semaphore limiter = this.getOrAddLimiterForPartitionKeyRange(request.getPartitionKeyRangeId());
        limiter.acquire();

        Mono<TransactionalBatchResponse> transactionalBatchResponse = this.docClientWrapper
            .executeBatchRequest(BridgeInternal.getLink(container), request, new RequestOptions(), false);

        return transactionalBatchResponse
            .map(sererResponse -> {
                // after execution release the limiter
                limiter.release();

                return new PartitionKeyRangeBatchExecutionResult(request.getPartitionKeyRangeId(), request.getOperations(), sererResponse);
            })
            .onErrorResume(throwable -> {
                limiter.release();
                return Mono.error(throwable);
            });
    }

    private static BatchPartitionKeyRangeGoneRetryPolicy getRetryPolicy(ThrottlingRetryOptions throttlingRetryOptions) {
        return new BatchPartitionKeyRangeGoneRetryPolicy(
            new ResourceThrottleRetryPolicy(
                throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
                throttlingRetryOptions.getMaxRetryWaitTime()));
    }

    private void reBatchAsync(ItemBatchOperation<?> operation) {

        CompletableFuture.runAsync(() -> {
            this.resolvePartitionKeyRangeIdAsync(operation).thenAccept(
                (String resolvedPartitionKeyRangeId) -> {
                    BatchAsyncStreamer streamer = this.getOrAddStreamerForPartitionKeyRange(resolvedPartitionKeyRangeId);
                    streamer.add(operation);
            });
        });
    }

    private CompletableFuture<String> resolvePartitionKeyRangeIdAsync(final ItemBatchOperation<?> operation) {

        checkNotNull(operation, "expected non-null operation");

        final RequestOptions options = operation.getRequestOptions();
        Object effectivePartitionKey = null;
        if (options != null) {
            final Map<String, Object> properties = options.getProperties();
            if (properties != null) {
                effectivePartitionKey = properties.getOrDefault(BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING, null);
            }
        }

        if (effectivePartitionKey != null) {
            throw new IllegalStateException("EPK is not supported");
        }

        String pkRangeId = null;
        DocumentCollection collection = this.container.getCollectionInfoAsync().block();

        if (collection != null) {
            PartitionKeyDefinition definition = collection.getPartitionKey();
            pkRangeId = this.docClientWrapper.getPartitionKeyRangeCache()
                .tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(null), collection.getResourceId(), null, null)
                .map((Utils.ValueHolder<CollectionRoutingMap> routingMap) -> {

                    PartitionKeyInternal partitionKeyInternal = getPartitionKeyInternal(operation, definition);
                    operation.setPartitionKeyJson(partitionKeyInternal.toJson());

                    return routingMap.v.getRangeByEffectivePartitionKey(
                        getEffectivePartitionKeyString(
                            partitionKeyInternal,
                            definition)).getId();
                }).block();
        }

        return CompletableFuture.completedFuture(pkRangeId);
    }

    private static void validateOperationEpk(
        final ItemBatchOperation<?> operation,
        final RequestOptions options) {

        checkNotNull(operation, "expected non-null operation");
        checkNotNull(options, "expected non-null options");

        final Map<String, Object> properties = options.getProperties();
        if (properties == null) {
            return;
        }

        if (properties.containsKey(BackendHeaders.EFFECTIVE_PARTITION_KEY)
            || properties.containsKey(BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING)
            || properties.containsKey(HttpHeaders.PARTITION_KEY)) {

            byte[] epk = (byte[]) properties.computeIfPresent(BackendHeaders.EFFECTIVE_PARTITION_KEY,
                (k, v) -> v instanceof byte[] ? v : null);

            String epkString = (String) properties.computeIfPresent(BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING,
                (k, v) -> v.getClass()  == String.class ? v : null);

            String pkString = (String) properties.computeIfPresent(HttpHeaders.PARTITION_KEY,
                (k, v) -> v.getClass() == String.class ? v : null);

            checkState(!((epk == null && pkString == null) || (epkString == null)),
                "expected byte array value for {0} and string value for {1} when either property is set",
                BackendHeaders.EFFECTIVE_PARTITION_KEY,
                BackendHeaders.EFFECTIVE_PARTITION_KEY_STRING);

            checkState(operation.getPartitionKey() == null,
                "partition key and effective partition key may not both be set.");
        }
    }

    private Semaphore getOrAddLimiterForPartitionKeyRange(String partitionKeyRangeId) {
        return this.limiters.computeIfAbsent(partitionKeyRangeId, id -> new Semaphore(1));
    }

    private BatchAsyncStreamer getOrAddStreamerForPartitionKeyRange(String partitionKeyRangeId) {

        return this.streamers.computeIfAbsent(partitionKeyRangeId, id -> new BatchAsyncStreamer(
            this.maxOperationCount,
            this.maxPayloadLength,
            this.timer,
            this.getOrAddLimiterForPartitionKeyRange(partitionKeyRangeId),
            DEFAULT_MAX_DEGREE_OF_CONCURRENCY,
            this::executeAsync,
            this::reBatchAsync));
    }

    private PartitionKeyInternal getPartitionKeyInternal(
        final ItemBatchOperation<?> operation,
        PartitionKeyDefinition partitionKeyDefinition) {

        checkNotNull(operation, "expected non-null operation");

        PartitionKey partitionKey = operation.getPartitionKey();

        if (partitionKey == null) {
            return ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
        } else {
            return BridgeInternal.getPartitionKeyInternal(partitionKey);
        }
    }

    /**
     * Closes the current {@link BatchAsyncContainerExecutor async batch executor}.
     */
    public final void close() {

        for (BatchAsyncStreamer streamer : this.streamers.values()) {
            streamer.close();
        }

        for (Semaphore limiter : this.limiters.values()) {
            limiter.drainPermits();
        }

        this.timer.stop();
        this.limiters.clear();
        this.streamers.clear();
    }
}
