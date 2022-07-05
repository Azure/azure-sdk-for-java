// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.WorkerSelector;

import java.util.List;
import java.util.Map;

/**
 * Request options to create a job.
 * Job: A unit of work to be routed.
 */
public class CreateJobOptions extends JobOptions {
    /**
     * Constructor for CreateJobOptions.
     * @param id The id of the job.
     * @param channelId The channel identifier. eg. voice, chat, etc.
     * @param queueId The Id of the Queue that this job is queued to.
     */
    public CreateJobOptions(String id, String channelId, String queueId) {
        this.id = id;
        this.channelId = channelId;
        this.queueId = queueId;
    }

    /**
     * Sets job priority.
     * @param priority The priority of this job.
     * @return this
     */
    public CreateJobOptions setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Sets channelReference.
     * @param channelReference Reference to an external parent context, eg. call ID.
     * @return this
     */
    public CreateJobOptions setChannelReference(String channelReference) {
        this.channelReference = channelReference;
        return this;
    }

    /**
     * Sets classificationPolicyId.
     * @param classificationPolicyId The Id of the Classification policy used for classifying a job.
     * @return this
     */
    public CreateJobOptions setClassificationPolicyId(String classificationPolicyId) {
        this.classificationPolicyId = classificationPolicyId;
        return this;
    }

    /**
     * Sets dispositionCode.
     * @param dispositionCode Reason code for cancelled or closed jobs.
     * @return this
     */
    public CreateJobOptions setDispositionCode(String dispositionCode) {
        this.dispositionCode = dispositionCode;
        return this;
    }

    /**
     * Sets requestedWorkerSelectors.
     * @param requestedWorkerSelectors A collection of manually specified label selectors, which a worker must
     *   satisfy in order to process this job.
     * @return this
     */
    public CreateJobOptions setRequestedWorkerSelectors(List<WorkerSelector> requestedWorkerSelectors) {
        this.requestedWorkerSelectors = requestedWorkerSelectors;
        return this;
    }

    /**
     * Sets labels.
     * @param labels A set of key/value pairs that are identifying attributes used by the
     *   rules engines to make decisions.
     * @return this
     */
    public CreateJobOptions setLabels(Map<String, Object> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Sets tags.
     * @param tags A set of non-identifying attributes attached to this job.
     * @return this
     */
    public CreateJobOptions setTags(Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Sets notes.
     * @param notes Notes attached to a job, sorted by timestamp.
     * @return this
     */
    public CreateJobOptions setNotes(Map<String, String> notes) {
        this.notes = notes;
        return this;
    }
}
