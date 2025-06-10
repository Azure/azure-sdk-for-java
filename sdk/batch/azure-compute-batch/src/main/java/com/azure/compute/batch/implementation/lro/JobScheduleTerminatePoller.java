// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.lro;

import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.models.BatchJobSchedule;
import com.azure.compute.batch.models.BatchJobScheduleState;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A helper class used by {@code beginTerminateJobSchedule} to implement polling logic
 * for terminating a {@link BatchJobSchedule}.
 */
public final class JobScheduleTerminatePoller {

    private final BatchClient batchClient;
    private final String jobScheduleId;
    private final RequestOptions options;
    private final Context requestContext;

    /**
     * Creates a new instance of {@link JobScheduleTerminatePoller}.
     *
     * @param batchClient The {@link BatchClient} used to communicate with the Azure Batch service.
     * @param jobScheduleId The ID of the job schedule to terminate.
     * @param options Optional request options to pass along with service calls.
     */
    public JobScheduleTerminatePoller(BatchClient batchClient, String jobScheduleId, RequestOptions options) {
        this.batchClient = batchClient;
        this.jobScheduleId = jobScheduleId;
        this.options = options;
        this.requestContext = options == null ? Context.NONE : options.getContext();
    }

    /**
     * Returns the activation operation that initiates termination of the job schedule.
     *
     * @return A function that performs the initial termination request and returns an {@link PollResponse}
     *         with status {@code IN_PROGRESS}.
     */
    public Function<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>> getActivationOperation() {
        return context -> {
            batchClient.terminateJobScheduleWithResponse(jobScheduleId, options);
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null);
        };
    }

    /**
     * Returns the polling operation that periodically checks the status of the job schedule termination.
     *
     * @return A function that polls the job schedule state and returns an appropriate {@link PollResponse}
     *         based on its current state.
     */
    public Function<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>> getPollOperation() {
        return context -> {
            try {
                RequestOptions pollOptions = new RequestOptions().setContext(this.requestContext);
                Response<BinaryData> response = batchClient.getJobScheduleWithResponse(jobScheduleId, pollOptions);
                BatchJobSchedule jobSchedule = response.getValue().toObject(BatchJobSchedule.class);
                BatchJobScheduleState state = jobSchedule.getState();

                LongRunningOperationStatus status;
                if (BatchJobScheduleState.TERMINATING.equals(state)) {
                    status = LongRunningOperationStatus.IN_PROGRESS;
                } else if (BatchJobScheduleState.COMPLETED.equals(state)) {
                    status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                } else {
                    status = LongRunningOperationStatus.FAILED;
                }

                return new PollResponse<>(status, jobSchedule);
            } catch (Exception e) {
                return new PollResponse<>(LongRunningOperationStatus.FAILED, null);
            }
        };
    }

    /**
     * Returns the cancel operation for the poller. This operation is not supported for job schedule termination
     * and will always return {@code null}.
     *
     * @return A {@link BiFunction} that returns {@code null}, indicating cancellation is not supported.
     */
    public BiFunction<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>, BatchJobSchedule>
        getCancelOperation() {
        return (context, pollResponse) -> null;
    }

    /**
     * Returns the fetch result operation, which provides the final result after the LRO completes.
     *
     * @return A function that retrieves the final {@link BatchJobSchedule} from the polling context.
     */
    public Function<PollingContext<BatchJobSchedule>, BatchJobSchedule> getFetchResultOperation() {
        return context -> {
            PollResponse<BatchJobSchedule> latestResponse = context.getLatestResponse();
            return latestResponse != null ? latestResponse.getValue() : null;
        };
    }
}
