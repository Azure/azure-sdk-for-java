// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.implementation.JsonMergePatchHelper;
import com.azure.communication.jobrouter.models.ClassificationPolicy;
import com.azure.communication.jobrouter.models.CreateClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateDistributionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.CreateJobOptions;
import com.azure.communication.jobrouter.models.CreateJobWithClassificationPolicyOptions;
import com.azure.communication.jobrouter.models.CreateQueueOptions;
import com.azure.communication.jobrouter.models.CreateWorkerOptions;
import com.azure.communication.jobrouter.models.DistributionPolicy;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.RouterJob;
import com.azure.communication.jobrouter.models.RouterQueue;
import com.azure.communication.jobrouter.models.RouterWorker;

/**
 * Utility class that converts request option bags to the model type that the option bag represents.
 */
public final class OptionBagAdapters {
    /**
     * Converts {@link CreateClassificationPolicyOptions} to {@link ClassificationPolicy}.
     * @param options Container with options to create a classification policy.
     * @return classification policy.
     */
    public static ClassificationPolicy toClassificationPolicy(CreateClassificationPolicyOptions options) {
        return new ClassificationPolicy()
            .setName(options.getName())
            .setPrioritizationRule(options.getPrioritizationRule())
            .setFallbackQueueId(options.getFallbackQueueId())
            .setQueueSelectorAttachments(options.getQueueSelectors())
            .setWorkerSelectorAttachments(options.getWorkerSelectors());
    }

    /**
     * Converts {@link CreateDistributionPolicyOptions} to {@link DistributionPolicy}.
     * @param createDistributionPolicyOptions Container with options to create a DistributionPolicy.
     * @return distribution policy.
     */
    public static DistributionPolicy toDistributionPolicy(
        CreateDistributionPolicyOptions createDistributionPolicyOptions) {
        DistributionPolicy policy = new DistributionPolicy()
            .setMode(createDistributionPolicyOptions.getMode())
            .setOfferExpiresAfter(createDistributionPolicyOptions.getOfferExpiresAfter())
            .setName(createDistributionPolicyOptions.getName());

        JsonMergePatchHelper.getDistributionPolicyAccessor()
            .setId(policy, createDistributionPolicyOptions.getDistributionPolicyId());

        return policy;
    }

    /**
     * Converts {@link CreateExceptionPolicyOptions} to {@link ExceptionPolicy}.
     * @param createExceptionPolicyOptions
     * @return exception policy.
     */
    public static ExceptionPolicy toExceptionPolicy(CreateExceptionPolicyOptions createExceptionPolicyOptions) {
        return new ExceptionPolicy()
            .setName(createExceptionPolicyOptions.getName())
            .setExceptionRules(createExceptionPolicyOptions.getExceptionRules());
    }

    /**
     * Converts {@link CreateJobOptions} to {@link RouterJob}.
     * @param createJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJob toRouterJob(CreateJobOptions createJobOptions) {
        return new RouterJob()
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setQueueId(createJobOptions.getQueueId())
            .setLabels(createJobOptions.getLabels())
            .setNotes(createJobOptions.getNotes())
            .setPriority(createJobOptions.getPriority())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setRequestedWorkerSelectors(createJobOptions.getRequestedWorkerSelectors())
            .setTags(createJobOptions.getTags())
            .setMatchingMode(createJobOptions.getMatchingMode());
    }

    /**
     * Converts {@link CreateJobWithClassificationPolicyOptions} to {@link RouterJob}.
     * @param createJobOptions Container with options to create {@link RouterJob}
     * @return RouterJob
     */
    public static RouterJob toRouterJob(CreateJobWithClassificationPolicyOptions createJobOptions) {
        return new RouterJob()
            .setClassificationPolicyId(createJobOptions.getClassificationPolicyId())
            .setChannelId(createJobOptions.getChannelId())
            .setChannelReference(createJobOptions.getChannelReference())
            .setQueueId(createJobOptions.getQueueId())
            .setLabels(createJobOptions.getLabels())
            .setNotes(createJobOptions.getNotes())
            .setPriority(createJobOptions.getPriority())
            .setDispositionCode(createJobOptions.getDispositionCode())
            .setRequestedWorkerSelectors(createJobOptions.getRequestedWorkerSelectors())
            .setTags(createJobOptions.getTags())
            .setMatchingMode(createJobOptions.getMatchingMode());
    }

    /**
     * Converts {@link CreateQueueOptions} to {@link RouterQueue}.
     * @param createQueueOptions Container with options to create {@link RouterQueue}
     * @return JobQueue
     */
    public static RouterQueue toRouterQueue(CreateQueueOptions createQueueOptions) {
        return new RouterQueue()
            .setName(createQueueOptions.getName())
            .setLabels(createQueueOptions.getLabels())
            .setDistributionPolicyId(createQueueOptions.getDistributionPolicyId())
            .setExceptionPolicyId(createQueueOptions.getExceptionPolicyId());
    }

    /**
     * Converts {@link CreateWorkerOptions} to {@link RouterWorker}.
     * @param createWorkerOptions Container with options to create {@link RouterWorker}
     * @return RouterWorker
     */
    public static RouterWorker toRouterWorker(CreateWorkerOptions createWorkerOptions) {
        return new RouterWorker()
            .setQueues(createWorkerOptions.getQueues())
            .setLabels(createWorkerOptions.getLabels())
            .setTags(createWorkerOptions.getTags())
            .setAvailableForOffers(createWorkerOptions.isAvailableForOffers())
            .setChannels(createWorkerOptions.getChannels())
            .setCapacity(createWorkerOptions.getCapacity())
            .setMaxConcurrentOffers(createWorkerOptions.getMaxConcurrentOffers());
    }
}
