// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.implementation.lro;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.compute.batch.BatchClient;
import com.azure.compute.batch.models.BatchJobSchedule;
import com.azure.compute.batch.models.BatchJobScheduleState;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.http.rest.RequestOptions;

import java.time.OffsetDateTime;
import java.util.function.Function;
import java.util.function.BiFunction;

/**
 * A helper class used by {@code beginDeleteJobSchedule} to implement polling logic for deleting a {@link BatchJobSchedule}.
 * This class tracks the initial creation time of the resource to detect if a new job schedule was created under the same ID
 * during polling. It returns {@link BatchJobSchedule} values during polling and {@code null} upon successful deletion.
 */
public final class JobScheduleDeletePoller {

    private final BatchClient batchClient;
    private final String jobScheduleId;
    private final RequestOptions options;
    private OffsetDateTime initialCreationTime;

    /**
     * Creates a new {@link JobScheduleDeletePoller}.
     *
     * @param batchClient The {@link BatchClient} used to interact with the Batch service.
     * @param jobScheduleId The ID of the Job Schedule being deleted.
     * @param options Optional request options for service calls.
     */
    public JobScheduleDeletePoller(BatchClient batchClient, String jobScheduleId, RequestOptions options) {
        this.batchClient = batchClient;
        this.jobScheduleId = jobScheduleId;
        this.options = options;
    }

    /**
     * Provides the activation operation that initiates the deletion of the job schedule.
     *
     * @return A function that starts the delete request and returns a {@link PollResponse} with IN_PROGRESS status.
     */
    public Function<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>> getActivationOperation() {
        return context -> {
            batchClient.deleteJobScheduleWithResponse(jobScheduleId, options);
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null);
        };
    }

    /**
     * Provides the polling operation to check the status of the delete operation.
     * It checks that the job schedule still exists, has the same creation time, and is in the DELETING state.
     *
     * @return A function that polls the job schedule and returns its status and current state.
     */
    public Function<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>> getPollOperation() {
        return context -> {
            try {
                Response<BinaryData> response = batchClient.getJobScheduleWithResponse(jobScheduleId, options);
                BatchJobSchedule jobSchedule = response.getValue().toObject(BatchJobSchedule.class);

                if (initialCreationTime == null) {
                    initialCreationTime = jobSchedule.getCreationTime();
                }

                boolean isSame = initialCreationTime.equals(jobSchedule.getCreationTime());
                boolean isDeleting = BatchJobScheduleState.DELETING.equals(jobSchedule.getState());

                if (isSame && isDeleting) {
                    return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, jobSchedule);
                } else {
                    return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, jobSchedule);
                }

            } catch (Exception e) {
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null);
            }
        };
    }

    /**
     * Returns the cancel operation for this LRO. Since cancellation is not supported for deleting job schedules,
     * this method returns {@code null}.
     *
     * @return A function that always returns {@code null} to indicate cancellation is unsupported.
     */
    public BiFunction<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>, BatchJobSchedule>
        getCancelOperation() {
        return (context, pollResponse) -> null;
    }

    /**
     * Returns the final result retrieval operation. Since no additional call is needed on completion,
     * this method returns {@code null}.
     *
     * @return A function that always returns {@code null}, indicating no final fetch is required.
     */
    public Function<PollingContext<BatchJobSchedule>, Void> getFetchResultOperation() {
        return context -> null;
    }
}
