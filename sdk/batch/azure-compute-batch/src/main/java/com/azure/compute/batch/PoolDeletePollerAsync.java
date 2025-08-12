// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchPool;
import com.azure.compute.batch.models.BatchPoolState;
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
 * Async poller class used by {@code beginDeletePool} to implement polling logic for deleting a {@link BatchPool}.
 * Returns {@link BatchPool} values during polling and {@code null} upon successful deletion.
 */
public final class PoolDeletePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String poolId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link PoolDeletePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param poolId The ID of the Pool being deleted.
     * @param options Optional request options for service calls.
     */
    public PoolDeletePollerAsync(BatchAsyncClient batchAsyncClient, String poolId, RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.poolId = poolId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the delete.
     *
     * @return A function that initiates the delete request and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getActivationOperation() {
        return context -> batchAsyncClient.deletePoolWithResponse(poolId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check if the pool is still being deleted or is gone.
     *
     * @return A function that polls the pool and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchPool>, Mono<PollResponse<BatchPool>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getPoolWithResponse(poolId, pollOptions).map(response -> {
                BatchPool pool = response.getValue().toObject(BatchPool.class);

                LongRunningOperationStatus status = BatchPoolState.DELETING.equals(pool.getState())
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                return new PollResponse<>(status, pool);
            })
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancel operation (not supported for delete).
     *
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchPool>, PollResponse<BatchPool>, Mono<BatchPool>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation (returns null; not required).
     *
     * @return A function that returns an empty Mono, indicating no final fetch is required.
     */
    public Function<PollingContext<BatchPool>, Mono<Void>> getFetchResultOperation() {
        return context -> Mono.empty();
    }
}
