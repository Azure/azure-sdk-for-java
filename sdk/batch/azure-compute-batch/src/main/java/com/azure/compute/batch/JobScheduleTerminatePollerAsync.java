// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchJobSchedule;
import com.azure.compute.batch.models.BatchJobScheduleState;
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
 * Async poller class used by {@code beginTerminateJobSchedule} to implement polling logic for terminating a {@link BatchJobSchedule}.
 * Returns {@link BatchJobSchedule} values during polling and as the final result.
 */
public final class JobScheduleTerminatePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String jobScheduleId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new {@link JobScheduleTerminatePollerAsync}.
     *
     * @param batchAsyncClient The {@link BatchAsyncClient} used to interact with the Batch service.
     * @param jobScheduleId The ID of the Job Schedule being terminated.
     * @param options Optional request options for service calls.
     */
    public JobScheduleTerminatePollerAsync(BatchAsyncClient batchAsyncClient, String jobScheduleId,
        RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.jobScheduleId = jobScheduleId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the termination.
     *
     * @return A function that initiates the terminate request and returns a PollResponse with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchJobSchedule>, Mono<PollResponse<BatchJobSchedule>>> getActivationOperation() {
        return context -> batchAsyncClient.terminateJobScheduleWithResponse(jobScheduleId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the job schedule's termination state.
     *
     * @return A function that polls the job schedule and returns a PollResponse with the current operation status.
     */
    public Function<PollingContext<BatchJobSchedule>, Mono<PollResponse<BatchJobSchedule>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobScheduleWithResponse(jobScheduleId, pollOptions).map(response -> {
                BatchJobSchedule jobSchedule = response.getValue().toObject(BatchJobSchedule.class);
                BatchJobScheduleState state = jobSchedule.getState();

                LongRunningOperationStatus status = BatchJobScheduleState.TERMINATING.equals(state)
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;

                return new PollResponse<>(status, jobSchedule);
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
     * @return A function that always returns an empty Mono, indicating cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>, Mono<BatchJobSchedule>>
        getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     *
     * @return A function that fetches the final {@link BatchJobSchedule} after successful termination.
     */
    public Function<PollingContext<BatchJobSchedule>, Mono<BatchJobSchedule>> getFetchResultOperation() {
        return context -> {
            RequestOptions fetchOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobScheduleWithResponse(jobScheduleId, fetchOptions)
                .map(response -> response.getValue().toObject(BatchJobSchedule.class));
        };
    }
}
