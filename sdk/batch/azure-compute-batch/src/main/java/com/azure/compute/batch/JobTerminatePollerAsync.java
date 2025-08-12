// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobState;
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
 * Async poller class used by {@code beginTerminateJob} to implement polling logic for terminating a {@link BatchJob}.
 * Returns {@link BatchJob} values during polling and as the final result.
 */
public final class JobTerminatePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String jobId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link JobTerminatePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param jobId The ID of the Job being terminated.
     * @param options Optional request options for service calls.
     */
    public JobTerminatePollerAsync(BatchAsyncClient batchAsyncClient, String jobId, RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.jobId = jobId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the termination.
     *
     * @return A function that initiates the terminate request and returns a {@link PollResponse}
     * with {@link LongRunningOperationStatus#IN_PROGRESS} status.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getActivationOperation() {
        return context -> batchAsyncClient.terminateJobWithResponse(jobId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the job's termination state.
     *
     * @return A function that polls the job and returns a {@link PollResponse}
     * with the current {@link LongRunningOperationStatus}.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, pollOptions).map(response -> {
                BatchJob job = response.getValue().toObject(BatchJob.class);
                BatchJobState state = job.getState();

                LongRunningOperationStatus status = BatchJobState.TERMINATING.equals(state)
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                return new PollResponse<>(status, job);
            })
                .onErrorResume(HttpResponseException.class,
                    ex -> ex.getResponse() != null && ex.getResponse().getStatusCode() == 404
                        ? Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null))
                        : Mono.error(ex));
        };
    }

    /**
     * Cancel operation (not supported for terminate).
     *
     * @return A function that always returns an empty {@link Mono}, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchJob>, PollResponse<BatchJob>, Mono<BatchJob>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that fetches the final {@link BatchJob} after successful termination.
     */
    public Function<PollingContext<BatchJob>, Mono<BatchJob>> getFetchResultOperation() {
        return context -> {
            RequestOptions fetchOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, fetchOptions)
                .map(response -> response.getValue().toObject(BatchJob.class));
        };
    }
}
