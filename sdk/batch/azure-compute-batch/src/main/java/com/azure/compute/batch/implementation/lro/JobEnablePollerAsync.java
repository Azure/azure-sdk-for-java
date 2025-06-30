package com.azure.compute.batch.implementation.lro;

import com.azure.compute.batch.BatchAsyncClient;
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
 * Async poller class used by {@code beginEnableJob} to implement polling logic for enabling a {@link BatchJob}.
 * Returns {@link BatchJob} values during polling and final {@link BatchJob} upon successful completion.
 */
public final class JobEnablePollerAsync {

    private final BatchAsyncClient batchAsyncClient;
    private final String jobId;
    private final RequestOptions options;
    private final Context requestContext;

    public JobEnablePollerAsync(BatchAsyncClient batchAsyncClient, String jobId, RequestOptions options) {
        this.batchAsyncClient = batchAsyncClient;
        this.jobId = jobId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getActivationOperation() {
        return context -> batchAsyncClient.enableJobWithResponse(jobId, options)
            .thenReturn(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
    }

    public Function<PollingContext<BatchJob>, Mono<PollResponse<BatchJob>>> getPollOperation() {
        return context -> {
            RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, pollOptions).map(response -> {
                BatchJob job = response.getValue().toObject(BatchJob.class);
                LongRunningOperationStatus status = BatchJobState.ENABLING.equals(job.getState())
                    ? LongRunningOperationStatus.IN_PROGRESS
                    : LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                return new PollResponse<>(status, job);
            })
                .onErrorResume(ResourceNotFoundException.class,
                    ex -> Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null)))
                .onErrorResume(e -> Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED, null)));
        };
    }

    public BiFunction<PollingContext<BatchJob>, PollResponse<BatchJob>, Mono<BatchJob>> getCancelOperation() {
        return (context, pollResponse) -> Mono.empty(); // Cancellation not supported
    }

    public Function<PollingContext<BatchJob>, Mono<BatchJob>> getFetchResultOperation() {
        return context -> {
            RequestOptions resultOptions = new RequestOptions().setContext(this.requestContext);
            return batchAsyncClient.getJobWithResponse(jobId, resultOptions)
                .map(response -> response.getValue().toObject(BatchJob.class));
        };
    }
}
