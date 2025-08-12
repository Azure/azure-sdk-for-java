// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.AllocationState;
import com.azure.compute.batch.models.BatchPool;
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
 * Async poller class used by {@code beginStopPoolResize} to implement polling
 * logic for halting a pool resize.  It emits {@link BatchPool} snapshots while
 * polling and returns the final {@link BatchPool} on successful completion.
 *
 * <p><b>Completion criteria</b></p>
 * <ul>
 *   <li>The pool’s {@link AllocationState} becomes {@code STEADY}; or</li>
 *   <li>The pool is no longer found (HTTP&nbsp;404) — treated as success with a
 *       {@code null} final result.</li>
 * </ul>
 */
public final class PoolStopResizePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link PoolStopResizePollerAsync}.
     *
     * @param batchAsyncClient Client used for Batch service calls.
     * @param poolId ID of the pool whose resize is being stopped.
     * @param options Optional {@link RequestOptions}; may be {@code null}.
     */
    public PoolStopResizePollerAsync(BatchAsyncClient batchAsyncClient, String poolId, RequestOptions options) {

        this.batchAsyncClient = batchAsyncClient;
        this.poolId = poolId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation that sends the initial <em>stop-resize</em> request.
     *
     * @return A function that initiates the operation and supplies a
     *         {@link PollResponse} whose status is
     *         {@link LongRunningOperationStatus#IN_PROGRESS}.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getActivationOperation() {
        return ctx -> batchAsyncClient
            .stopPoolResizeWithResponse(poolId, options == null ? new RequestOptions() : options)
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
                AllocationState state = pool.getAllocationState();

                LongRunningOperationStatus status = AllocationState.STEADY.equals(state)
                    ? LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    : LongRunningOperationStatus.IN_PROGRESS;

                return Mono.just(new PollResponse<>(status, pool));
            })
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancellation operation — not supported for stop-resize.
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
     * @return A function that retrieves the final {@link BatchPool} stored in
     *         the latest poll response, or {@link Mono#empty()} if the pool
     *         no longer exists.
     */
    public Function<PollingContext<BatchPool>, Mono<BatchPool>> getFetchResultOperation() {
        return ctx -> Mono.justOrEmpty(ctx.getLatestResponse().getValue());
    }
}
