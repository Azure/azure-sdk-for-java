// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchNode;
import com.azure.compute.batch.models.BatchNodeState;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Async poller class used by {@code beginStartNode} to implement polling logic for starting a {@link BatchNode}.
 * Returns {@link BatchNode} values during polling and the final {@link BatchNode} upon successful completion.
 */
public final class NodeStartPollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final String nodeId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link NodeStartPollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param poolId The ID of the pool that contains the node.
     * @param nodeId The ID of the node to start.
     * @param options Optional request options for service calls.
     */
    public NodeStartPollerAsync(BatchAsyncClient batchAsyncClient, String poolId, String nodeId,
        RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.poolId = poolId;
        this.nodeId = nodeId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the node.
     *
     * @return A function that initiates the start request and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchNode>, Mono<PollResponse<BatchNode>>> getActivationOperation() {
        return ctx -> batchAsyncClient.startNodeWithResponse(poolId, nodeId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the node's start-up state.
     *
     * @return A function that polls the node and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchNode>, Mono<PollResponse<BatchNode>>> getPollOperation() {
        return ctx -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getNodeWithResponse(poolId, nodeId, pollOptions).flatMap(resp -> {
                BatchNode node = resp.getValue().toObject(BatchNode.class);
                BatchNodeState state = node.getState();

                LongRunningOperationStatus status = (state == BatchNodeState.STARTING)
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                return Mono.just(new PollResponse<>(status, node));
            });
        };
    }

    /**
     * Cancel operation (not supported for start).
     *
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchNode>, PollResponse<BatchNode>, Mono<BatchNode>> getCancelOperation() {
        return (ctx, resp) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that returns the final {@link BatchNode} result from the latest poll response.
     */
    public Function<PollingContext<BatchNode>, Mono<BatchNode>> getFetchResultOperation() {
        return ctx -> Mono.justOrEmpty(ctx.getLatestResponse().getValue());
    }
}
