package com.azure.compute.batch;

import com.azure.compute.batch.models.BatchJob;
import com.azure.compute.batch.models.BatchJobState;
import com.azure.core.exception.ResourceNotFoundException;
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

    public JobTerminatePollerAsync(BatchAsyncClient batchAsyncClient, String jobId, RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.jobId = jobId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Activation operation to start the termination.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getActivationOperation() {
        return context -> batchAsyncClient.terminateJobWithResponse(jobId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    /**
     * Poll operation to check the job's termination state.
     */
    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, pollOptions).flatMap(response -> {
                BatchJob job = response.getValue().toObject(BatchJob.class);
                BatchJobState state = job.getState();

                LongRunningOperationStatus status;
                if (BatchJobState.TERMINATING.equals(state)) {
                    status = LongRunningOperationStatus.IN_PROGRESS;
                } else if (BatchJobState.COMPLETED.equals(state)) {
                    status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                } else {
                    status = LongRunningOperationStatus.FAILED;
                }

                return Mono.just(new PollResponse<>(status, job));
            })
                .onErrorResume(ResourceNotFoundException.class,
                    ex -> Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null)))
                .onErrorResume(e -> Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null)));
        };
    }

    /**
     * Cancel operation (not supported for terminate).
     */
    public BiFunction<PollingContext<BatchJob>, PollResponse<BatchJob>, Mono<BatchJob>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty();
    }

    /**
     * Final result fetch operation.
     */
    public Function<PollingContext<BatchJob>, Mono<BatchJob>> getFetchResultOperation() {
        return context -> {
            RequestOptions fetchOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, fetchOptions)
                .map(response -> response.getValue().toObject(BatchJob.class));
        };
    }
}
