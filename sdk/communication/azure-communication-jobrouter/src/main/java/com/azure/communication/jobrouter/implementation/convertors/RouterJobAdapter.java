// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.implementation.convertors;

import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.UpdateJobOptions;

/**
 * Converts request options for create and update Classification Policy to {@link RouterJob}.
 */
public class RouterJobAdapter {

    /**
     * Constructor for CreateJobOptions.
     * @param createJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJob convertCreateJobOptionsToRouterJob(CreateJobOptions createJobOptions) {
        return new RouterJob()
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setLabels(createJobOptions.getLabels())
            .setNotes(createJobOptions.getNotes())
            .setPriority(createJobOptions.getPriority())
            .setClassificationPolicyId(createJobOptions.getClassificationPolicyId())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setClassificationPolicyId(createJobOptions.getClassificationPolicyId())
            .setRequestedWorkerSelectors(createJobOptions.getRequestedWorkerSelectors())
            .setTags(createJobOptions.getTags());
    }

    /**
     * Constructor for UpdateJobOptions.
     * @param updateJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJob convertUpdateJobOptionsToRouterJob(UpdateJobOptions updateJobOptions) {
        return new RouterJob()
            .setChannelId(updateJobOptions.getChannelId())
            .setChannelReference(updateJobOptions.getChannelReference())
            .setLabels(updateJobOptions.getLabels())
            .setNotes(updateJobOptions.getNotes())
            .setPriority(updateJobOptions.getPriority())
            .setClassificationPolicyId(updateJobOptions.getClassificationPolicyId())
            .setDispositionCode(updateJobOptions.getDispositionCode())
            .setClassificationPolicyId(updateJobOptions.getClassificationPolicyId())
            .setRequestedWorkerSelectors(updateJobOptions.getRequestedWorkerSelectors())
            .setTags(updateJobOptions.getTags());
    }
}
