// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.AllocationState;
import com.azure.compute.batch.models.BatchNodeRemoveParameters;
import com.azure.compute.batch.models.BatchPool;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Async poller used by {@code beginRemoveNodes} to drive the *remove nodes*
 * long-running operation.
 *
 * <p>The poller completes when the pool’s {@link AllocationState} returns to
 * {@code steady}. A {@code 404} (pool deleted) is also treated as successful
 * completion, returning {@code null} as the final result.</p>
 */
public final class NodeRemovePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final BatchNodeRemoveParameters parameters;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link NodeRemovePollerAsync}.
     *
     * @param client Batch async client.
     * @param poolId ID of the pool from which nodes are being removed.
     * @param parameters Body parameters (list of node IDs, deallocation option, …).
     * @param options Optional request options (may be {@code null}).
     */
    public NodeRemovePollerAsync(BatchAsyncClient client, String poolId, BatchNodeRemoveParameters parameters,
        RequestOptions options) {
        this.batchAsyncClient = client;
        this.poolId = poolId;
        this.parameters = parameters;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation that sends the initial <em>remove-nodes</em> request.
     *
     * @return A function that initiates the removal and returns a
     *         {@link PollResponse} whose status is
     *         {@link LongRunningOperationStatus#IN_PROGRESS}.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getActivationOperation() {
        return ctx -> batchAsyncClient.removeNodesWithResponse(poolId, BinaryData.fromObject(parameters), options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the pool's state.
     *
     * @return A function that polls the pool and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getPollOperation() {
        return ctx -> {
            RequestOptions pollOpts = new RequestOptions().setContext(requestContext);

            return batchAsyncClient.getPoolWithResponse(poolId, pollOpts).flatMap(resp -> {
                BatchPool pool = resp.getValue().toObject(BatchPool.class);
                AllocationState aState = pool.getAllocationState();

                LongRunningOperationStatus status = (aState == AllocationState.STEADY)
                    ? LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    : LongRunningOperationStatus.IN_PROGRESS;

                return Mono.just(new PollResponse<>(status, pool));
            })
                // Pool gone (e.g. deleted while resizing) ⇒ treat as success, no final value.
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancel operation (not supported for remove-nodes).
     *
     * @return A function that always returns {@link Mono#empty()}, indicating
     *         cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchPool>, PollResponse<BatchPool>, Mono<BatchPool>> getCancelOperation() {
        return (ctx, resp) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that returns the final {@link BatchPool} stored in the
     *         latest poll response, or {@link Mono#empty()} if the pool no longer exists.
     */
    public Function<PollingContext<BatchPool>, Mono<BatchPool>> getFetchResultOperation() {
        return ctx -> Mono.justOrEmpty(ctx.getLatestResponse().getValue());
    }
}
