// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.JobMatchingModeInternal;
import com.azure.communication.jobrouter.implementation.models.QueueAndMatchModeInternal;
import com.azure.communication.jobrouter.implementation.models.RouterJobInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerSelectorInternal;
import com.azure.communication.jobrouter.implementation.models.ScheduleAndSuspendModeInternal;
import com.azure.communication.jobrouter.implementation.models.SuspendModeInternal;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.JobMatchingMode;
import com.azure.communication.jobrouter.models.QueueAndMatchMode;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobNote;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.ScheduleAndSuspendMode;
import com.azure.communication.jobrouter.models.SuspendMode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.communication.jobrouter.implementation.converters.RouterValueAdapter.getValue;

/**
 * Converts request options for create and update Job to {@link RouterJob}.
 */
public class JobAdapter {

    /**
     * Converts {@link CreateJobOptions} to {@link RouterJob}.
     * @param createJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJobInternal convertCreateJobOptionsToRouterJob(CreateJobOptions createJobOptions) {
        Map<String, RouterValue> labelValueMap = createJobOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        Map<String, RouterValue> tagValueMap = createJobOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        List<RouterJobNote> jobNotes = createJobOptions.getNotes();
        List<RouterWorkerSelectorInternal> workerSelectors = createJobOptions.getRequestedWorkerSelectors()
            .stream()
            .map(workerSelector ->
                new RouterWorkerSelectorInternal(workerSelector.getKey(), workerSelector.getLabelOperator())
                    .setValue(getValue(workerSelector.getValue()))
                    .setExpedite(workerSelector.isExpedite())
                    .setExpiresAt(workerSelector.getExpiresAt())
                    .setExpiresAfterSeconds((double) workerSelector.getExpiresAfter().getSeconds())
                    .setStatus(workerSelector.getStatus())
            )
            .collect(Collectors.toList());
        String jobMatchingModeKind = createJobOptions.getMatchingMode().getKind();
        JobMatchingModeInternal jobMatchingModeInternal = null;

        switch (jobMatchingModeKind) {
            case "scheduleAndSuspend":
                ScheduleAndSuspendMode scheduleAndSuspendMode = (ScheduleAndSuspendMode) createJobOptions.getMatchingMode();
                jobMatchingModeInternal = new ScheduleAndSuspendModeInternal(scheduleAndSuspendMode.getScheduleAt());
                break;
            case "queueAndMatch":
                jobMatchingModeInternal = new QueueAndMatchModeInternal();
                break;
            case "suspend":
                jobMatchingModeInternal = new SuspendModeInternal();
                break;
            default:
                throw new IllegalStateException("Unknown kind for JobMatchingMode.");
        }


        return new RouterJobInternal()
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setQueueId(createJobOptions.getQueueId())
            .setLabels(labels)
            .setNotes(jobNotes)
            .setPriority(createJobOptions.getPriority())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setRequestedWorkerSelectors(workerSelectors)
            .setTags(tags)
            .setMatchingMode(jobMatchingModeInternal);
    }

    /**
     * Converts jobMatchingMode internal to external
     * @param jobMatchingModeInternal internal model.
     * @return JobMatchingMode.
     */
    public static JobMatchingMode convertJobMatchingModeToPublic(JobMatchingModeInternal jobMatchingModeInternal) {
        if (jobMatchingModeInternal.getClass() == ScheduleAndSuspendModeInternal.class) {
            ScheduleAndSuspendModeInternal scheduleAndSuspendModeInternal =
                (ScheduleAndSuspendModeInternal) jobMatchingModeInternal;
            return new ScheduleAndSuspendMode(scheduleAndSuspendModeInternal.getScheduleAt());
        } else if (jobMatchingModeInternal.getClass() == QueueAndMatchModeInternal.class) {
            return new QueueAndMatchMode();
        } else if (jobMatchingModeInternal.getClass() == SuspendModeInternal.class) {
            return new SuspendMode();
        }
        throw new IllegalStateException(String.format("Unknown type of jobMatchingMode %s", jobMatchingModeInternal.getClass().getTypeName()));
    }
}
