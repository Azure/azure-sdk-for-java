// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.options.CreateJobOptions;
import com.azure.communication.jobrouter.models.options.UpdateJobOptions;

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
        Map<String, Object> labels = labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return new RouterJob()
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setLabels(labels)
            .setNotes(createJobOptions.getNotes())
            .setPriority(createJobOptions.getPriority())
            .setClassificationPolicyId(createJobOptions.getClassificationPolicyId())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setClassificationPolicyId(createJobOptions.getClassificationPolicyId())
            .setRequestedWorkerSelectors(createJobOptions.getRequestedWorkerSelectors())
            .setTags(createJobOptions.getTags());
    }

    /**
     * Converts {@link UpdateJobOptions} to {@link RouterJob}.
     * @param updateJobOptions Container with options to update {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJob convertUpdateJobOptionsToRouterJob(UpdateJobOptions updateJobOptions) {
        Map<String, LabelValue> labelValueMap = updateJobOptions.getLabels();
        Map<String, Object> labels = labelValueMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
        return new RouterJob()
            .setChannelId(updateJobOptions.getChannelId())
            .setChannelReference(updateJobOptions.getChannelReference())
            .setLabels(labels)
            .setNotes(updateJobOptions.getNotes())
            .setPriority(updateJobOptions.getPriority())
            .setClassificationPolicyId(updateJobOptions.getClassificationPolicyId())
            .setDispositionCode(updateJobOptions.getDispositionCode())
            .setClassificationPolicyId(updateJobOptions.getClassificationPolicyId())
            .setRequestedWorkerSelectors(updateJobOptions.getRequestedWorkerSelectors())
            .setTags(updateJobOptions.getTags());
    }
}
