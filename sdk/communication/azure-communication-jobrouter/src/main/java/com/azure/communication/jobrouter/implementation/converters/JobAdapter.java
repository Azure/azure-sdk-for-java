// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.models.RouterJobInternal;
import com.azure.communication.jobrouter.implementation.models.RouterWorkerSelectorInternal;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterJobNote;

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
            .setMatchingMode(createJobOptions.getMatchingMode());
    }

    private static Object getValue(RouterValue routerValue) {
        if (routerValue.getValueAsBoolean()) {
            return routerValue.getValueAsBoolean();
        } else if (routerValue.getValueAsDouble() != null) {
            return routerValue.getValueAsDouble();
        } else if (routerValue.getValueAsInteger() != null) {
            return routerValue.getValueAsInteger();
        } else if (routerValue.getValueAsString() != null) {
            return routerValue.getValueAsString();
        }
        return null;
    }
}
