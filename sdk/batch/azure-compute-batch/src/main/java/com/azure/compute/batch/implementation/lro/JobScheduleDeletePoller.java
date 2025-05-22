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

public final class JobScheduleDeletePoller {

    private final BatchClient batchClient;
    private final String jobScheduleId;
    private final RequestOptions options;
    private OffsetDateTime initialCreationTime;

    public JobScheduleDeletePoller(BatchClient batchClient, String jobScheduleId, RequestOptions options) {
        this.batchClient = batchClient;
        this.jobScheduleId = jobScheduleId;
        this.options = options;
    }

    public Function<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>> getActivationOperation() {
        return context -> {
            batchClient.deleteJobScheduleWithResponse(jobScheduleId, options);
            return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null);
        };
    }

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

    public BiFunction<PollingContext<BatchJobSchedule>, PollResponse<BatchJobSchedule>, BatchJobSchedule>
        getCancelOperation() {
        return (context, pollResponse) -> null;
    }

    public Function<PollingContext<BatchJobSchedule>, Void> getFetchResultOperation() {
        return context -> null;
    }
}
