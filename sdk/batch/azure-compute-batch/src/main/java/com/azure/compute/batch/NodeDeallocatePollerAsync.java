// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchNode;
import com.azure.compute.batch.models.BatchNodeState;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Async poller class used by {@code beginDeallocateNode} to implement polling logic for deallocating a {@link BatchNode}.
 * Returns {@link BatchNode} values during polling and the final {@link BatchNode} upon successful completion.
 * <p>
 * If the node is no longer found (e.g., due to pool deletion), the operation is treated as successfully completed.
 */
public final class NodeDeallocatePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final String nodeId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link NodeDeallocatePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param poolId The ID of the pool that contains the node.
     * @param nodeId The ID of the node to deallocate.
     * @param options Optional request options for service calls.
     */
    public NodeDeallocatePollerAsync(BatchAsyncClient batchAsyncClient, String poolId, String nodeId,
        RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.poolId = poolId;
        this.nodeId = nodeId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the deallocate process.
     *
     * @return A function that initiates the deallocation and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchNode>, Mono<PollResponse<BatchNode>>> getActivationOperation() {
        return context -> batchAsyncClient.deallocateNodeWithResponse(poolId, nodeId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the node's state.
     *
     * @return A function that polls the node and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchNode>, Mono<PollResponse<BatchNode>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getNodeWithResponse(poolId, nodeId, pollOptions).map(response -> {
                BatchNode node = response.getValue().toObject(BatchNode.class);
                BatchNodeState state = node.getState();

                LongRunningOperationStatus status = BatchNodeState.DEALLOCATING.equals(state)
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                return new PollResponse<>(status, node);
            })
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancel operation (not supported for deallocate).
     *
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchNode>, PollResponse<BatchNode>, Mono<BatchNode>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that returns the final {@link BatchNode} result from the latest poll response.
     */
    public Function<PollingContext<BatchNode>, Mono<BatchNode>> getFetchResultOperation() {
        return context -> Mono.justOrEmpty(context.getLatestResponse().getValue());
    }
}
