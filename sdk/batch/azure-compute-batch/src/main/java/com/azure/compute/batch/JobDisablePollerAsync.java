// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobDisableParameters;
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
 * Async poller class used by {@code beginDisableJob} to implement polling logic for disabling a {@link BatchJob}.
 * Returns {@link BatchJob} values during polling and as the final result.
 */
public final class JobDisablePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String jobId;
    private final BatchJobDisableParameters parameters;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link JobDisablePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param jobId The ID of the Job being disabled.
     * @param parameters The options to use when disabling the Job.
     * @param options Optional request options for service calls.
     */
    public JobDisablePollerAsync(BatchAsyncClient batchAsyncClient, String jobId, BatchJobDisableParameters parameters,
        RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.jobId = jobId;
        this.parameters = parameters;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the disable.
     *
     * @return A function that initiates the disable request and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getActivationOperation() {
        return context -> batchAsyncClient
            .disableJobWithResponse(jobId, com.azure.core.util.BinaryData.fromObject(parameters), options)
            .then(batchAsyncClient.getJobWithResponse(jobId, null))
            .map(response -> {
                BatchJob job = response.getValue().toObject(BatchJob.class);
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, job);
            });
    }

    /**
     * Poll operation to check the job's disablement state.
     *
     * @return A function that polls the Job and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, pollOptions).map(response -> {
                BatchJob job = response.getValue().toObject(BatchJob.class);
                BatchJobState state = job.getState();

                LongRunningOperationStatus status = BatchJobState.DISABLING.equals(state)
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                return new PollResponse<>(status, job);
            });
        };
    }

    /**
     * Cancel operation (not supported for disable).
     *
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchJob>, PollResponse<BatchJob>, Mono<BatchJob>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that fetches the final {@link BatchJob} after successful disablement.
     */
    public Function<PollingContext<BatchJob>, Mono<BatchJob>> getFetchResultOperation() {
        return context -> {
            RequestOptions fetchOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, fetchOptions)
                .map(response -> response.getValue().toObject(BatchJob.class));
        };
    }
}
