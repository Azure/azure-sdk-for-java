// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobState;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Async poller class used by {@code beginEnableJob} to implement polling logic for enabling a {@link BatchJob}.
 * Returns {@link BatchJob} values during polling and the final {@link BatchJob} upon successful completion.
 */
public final class JobEnablePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String jobId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link JobEnablePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param jobId The ID of the Job being enabled.
     * @param options Optional request options for service calls.
     */
    public JobEnablePollerAsync(BatchAsyncClient batchAsyncClient, String jobId, RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.jobId = jobId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the enable process.
     *
     * @return A function that initiates the enable request and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getActivationOperation() {
        return context -> batchAsyncClient.enableJobWithResponse(jobId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the job's enablement state.
     *
     * @return A function that polls the Job and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, pollOptions).map(response -> {
                BatchJob job = response.getValue().toObject(BatchJob.class);
                LongRunningOperationStatus status = BatchJobState.ENABLING.equals(job.getState())
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                return new PollResponse<>(status, job);
            });
        };
    }

    /**
     * Cancel operation (not supported for enable).
     *
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchJob>, PollResponse<BatchJob>, Mono<BatchJob>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that fetches the final {@link BatchJob} after successful enablement.
     */
    public Function<PollingContext<BatchJob>, Mono<BatchJob>> getFetchResultOperation() {
        return context -> {
            RequestOptions resultOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, resultOptions)
                .map(response -> response.getValue().toObject(BatchJob.class));
        };
    }
}
