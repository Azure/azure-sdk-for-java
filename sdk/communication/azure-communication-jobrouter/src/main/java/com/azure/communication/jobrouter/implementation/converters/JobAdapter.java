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
import com.azure.communication.jobrouter.models.CreateJobWithClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.JobMatchingModeKind;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobNote;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.ScheduleAndSuspendMode;

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
        Map<String, RouterValue> tagValueMap = createJobOptions.getTags();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        List<RouterJobNote> jobNotes = createJobOptions.getNotes();
        List<RouterWorkerSelectorInternal> workerSelectors;
        workerSelectors = createJobOptions.getRequestedWorkerSelectors() != null ? createJobOptions.getRequestedWorkerSelectors()
            .stream()
            .map(workerSelector ->
                new RouterWorkerSelectorInternal().setKey(workerSelector.getKey()).setLabelOperator(workerSelector.getLabelOperator())
                    .setValue(getValue(workerSelector.getValue()))
                    .setExpedite(workerSelector.isExpedite())
                    .setExpiresAt(workerSelector.getExpiresAt())
                    .setExpiresAfterSeconds(workerSelector.getExpiresAfter() != null ? Double.valueOf(workerSelector.getExpiresAfter().getSeconds()) : null)
                    .setStatus(workerSelector.getStatus())
            )
            .collect(Collectors.toList()) : null;
        JobMatchingModeKind jobMatchingModeKind;
        jobMatchingModeKind = createJobOptions.getMatchingMode() != null ? createJobOptions.getMatchingMode().getKind() : null;
        JobMatchingModeInternal jobMatchingModeInternal = null;

        if (jobMatchingModeKind != null) {
            if (jobMatchingModeKind.equals(JobMatchingModeKind.SCHEDULE_AND_SUSPEND)) {
                ScheduleAndSuspendMode scheduleAndSuspendMode = (ScheduleAndSuspendMode) createJobOptions.getMatchingMode();
                jobMatchingModeInternal = new ScheduleAndSuspendModeInternal().setScheduleAt(scheduleAndSuspendMode.getScheduleAt());
            } else if (jobMatchingModeKind.equals(JobMatchingModeKind.QUEUE_AND_MATCH)) {
                jobMatchingModeInternal = new QueueAndMatchModeInternal();
            } else if (jobMatchingModeKind.equals(JobMatchingModeKind.SUSPEND)) {
                jobMatchingModeInternal = new SuspendModeInternal();
            } else {
                throw new IllegalStateException("Unknown kind for JobMatchingMode.");
            }
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
     * Converts {@link CreateJobWithClassificationPolicyOptions} to {@link RouterJob}.
     * @param createJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJobInternal convertCreateJobWithClassificationPolicyOptionsToRouterJob(CreateJobWithClassificationPolicyOptions createJobOptions) {
        Map<String, RouterValue> labelValueMap = createJobOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        Map<String, RouterValue> tagValueMap = createJobOptions.getTags();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        List<RouterJobNote> jobNotes = createJobOptions.getNotes();
        List<RouterWorkerSelectorInternal> workerSelectors;
        workerSelectors = createJobOptions.getRequestedWorkerSelectors() != null ? createJobOptions.getRequestedWorkerSelectors()
            .stream()
            .map(workerSelector ->
                new RouterWorkerSelectorInternal().setKey(workerSelector.getKey()).setLabelOperator(workerSelector.getLabelOperator())
                    .setValue(getValue(workerSelector.getValue()))
                    .setExpedite(workerSelector.isExpedite())
                    .setExpiresAt(workerSelector.getExpiresAt())
                    .setExpiresAfterSeconds(workerSelector.getExpiresAfter() != null ? Double.valueOf(workerSelector.getExpiresAfter().getSeconds()) : null)
                    .setStatus(workerSelector.getStatus())
            )
            .collect(Collectors.toList()) : null;
        JobMatchingModeKind jobMatchingModeKind;
        jobMatchingModeKind = createJobOptions.getMatchingMode() != null ? createJobOptions.getMatchingMode().getKind() : null;
        JobMatchingModeInternal jobMatchingModeInternal = null;

        if (jobMatchingModeKind != null) {
            if (jobMatchingModeKind.equals(JobMatchingModeKind.SCHEDULE_AND_SUSPEND)) {
                ScheduleAndSuspendMode scheduleAndSuspendMode = (ScheduleAndSuspendMode) createJobOptions.getMatchingMode();
                jobMatchingModeInternal = new ScheduleAndSuspendModeInternal().setScheduleAt(scheduleAndSuspendMode.getScheduleAt());
            } else if (jobMatchingModeKind.equals(JobMatchingModeKind.QUEUE_AND_MATCH)) {
                jobMatchingModeInternal = new QueueAndMatchModeInternal();
            } else if (jobMatchingModeKind.equals(JobMatchingModeKind.SUSPEND)) {
                jobMatchingModeInternal = new SuspendModeInternal();
            } else {
                throw new IllegalStateException("Unknown kind for JobMatchingMode.");
            }
        }

        return new RouterJobInternal()
            .setClassificationPolicyId(createJobOptions.getClassificationPolicyId())
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
}
