// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobNote;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts request options for create and update Job to {@link RouterJob}.
 */
public class JobAdapter {

    /**
     * Converts {@link CreateJobOptions} to {@link RouterJob}.
     * @param createJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJob convertCreateJobOptionsToRouterJob(CreateJobOptions createJobOptions) {
        Map<String, LabelValue> labelValueMap = createJobOptions.getLabels();
        Map<String, Object> labels = labelValueMap != null ? labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        Map<String, LabelValue> tagValueMap = createJobOptions.getLabels();
        Map<String, Object> tags = tagValueMap != null ? tagValueMap.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> getValue(entry.getValue()))) : null;
        List<RouterWorkerSelector> workerSelectors = createJobOptions.getRequestedWorkerSelectors();
        List<RouterJobNote> jobNotes = createJobOptions.getNotes();

        return new RouterJob()
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setQueueId(createJobOptions.getQueueId())
            .setLabels(labels)
            .setNotes(jobNotes)
            .setPriority(createJobOptions.getPriority())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setRequestedWorkerSelectors(workerSelectors)
            .setTags(tags)
            .setMatchingMode(createJobOptions.getMatchingMode());
    }

    private static Object getValue(LabelValue labelValue) {
        if (labelValue.getValueAsBoolean()) {
            return labelValue.getValueAsBoolean();
        } else if (labelValue.getValueAsDouble() != null) {
            return labelValue.getValueAsDouble();
        } else if (labelValue.getValueAsInteger() != null) {
            return labelValue.getValueAsInteger();
        } else if (labelValue.getValueAsString() != null) {
            return labelValue.getValueAsString();
        }
        return null;
    }
}
