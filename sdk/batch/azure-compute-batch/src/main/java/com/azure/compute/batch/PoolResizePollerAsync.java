// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.AllocationState;
import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolResizeParameters;
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
 * Async poller class used by {@code beginResizePool} to implement polling logic for resizing a {@link BatchPool}.
 * Returns {@link BatchPool} values during polling and the final {@link BatchPool} upon successful completion.
 */
public final class PoolResizePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final BatchPoolResizeParameters parameters;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link PoolResizePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param poolId The ID of the pool being resized.
     * @param parameters The resize parameters.
     * @param options Optional request options for service calls.
     */
    public PoolResizePollerAsync(BatchAsyncClient batchAsyncClient, String poolId, BatchPoolResizeParameters parameters,
        RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.poolId = poolId;
        this.parameters = parameters;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the resize.
     *
     * @return A function that initiates the resize request and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getActivationOperation() {
        return ctx -> batchAsyncClient
            .resizePoolWithResponse(poolId, com.azure.core.util.BinaryData.fromObject(parameters), options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the pool's allocation state.
     *
     * @return A function that polls the pool and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getPollOperation() {
        return ctx -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getPoolWithResponse(poolId, pollOptions).map(resp -> {
                BatchPool pool = resp.getValue().toObject(BatchPool.class);
                AllocationState allocation = pool.getAllocationState();

                LongRunningOperationStatus status = AllocationState.STEADY.equals(allocation)
                    ? LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    : LongRunningOperationStatus.IN_PROGRESS;

                return new PollResponse<>(status, pool);
            })
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancel operation (not supported for resize).
     *
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchPool>, PollResponse<BatchPool>, Mono<BatchPool>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that returns the final {@link BatchPool} result from the latest poll response.
     */
    public Function<PollingContext<BatchPool>, Mono<BatchPool>> getFetchResultOperation() {
        return ctx -> Mono.justOrEmpty(ctx.getLatestResponse().getValue());
    }
}
