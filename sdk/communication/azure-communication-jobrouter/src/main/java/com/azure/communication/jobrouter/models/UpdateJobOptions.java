// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.util.List;
import java.util.Map;

/**
 * Request options to update a job.
 * Job: A unit of work to be routed.
 */
public class UpdateJobOptions extends JobOptions {

    /**
     * Constructor for UpdateJobOptions.
     * @param id The id of the job.
     */
    public UpdateJobOptions(String id) {
        this.id = id;
    }

    /**
     * Sets channelReference.
     * @param channelReference Reference to an external parent context, eg. call ID.
     * @return this
     */
    public UpdateJobOptions setChannelReference(String channelReference) {
        this.channelReference = channelReference;
        return this;
    }

    /**
     * Sets channelId.
     * @param channelId The channel identifier. eg. voice, chat, etc.
     * @return this
     */
    public UpdateJobOptions setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Sets queueId.
     * @param queueId The Id of the Queue that this job is queued to.
     * @return this
     */
    public UpdateJobOptions setQueueId(String queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * Sets priority.
     * @param priority The priority of this job.
     * @return this
     */
    public UpdateJobOptions setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Sets classificationPolicyId.
     * @param classificationPolicyId The Id of the Classification policy used for classifying a job.
     * @return this
     */
    public UpdateJobOptions setClassificationPolicyId(String classificationPolicyId) {
        this.classificationPolicyId = classificationPolicyId;
        return this;
    }

    /**
     * Sets dispositionCode.
     * @param dispositionCode Reason code for cancelled or closed jobs.
     * @return this
     */
    public UpdateJobOptions setDispositionCode(String dispositionCode) {
        this.dispositionCode = dispositionCode;
        return this;
    }

    /**
     * Sets requestedWorkerSelectors.
     * @param requestedWorkerSelectors A collection of manually specified label selectors, which a worker must
     *   satisfy in order to process this job.
     * @return this
     */
    public UpdateJobOptions setRequestedWorkerSelectors(List<WorkerSelector> requestedWorkerSelectors) {
        this.requestedWorkerSelectors = requestedWorkerSelectors;
        return this;
    }

    /**
     * Sets labels.
     * @param labels A set of key/value pairs that are identifying attributes used by the
     *   rules engines to make decisions.
     * @return this
     */
    public UpdateJobOptions setLabels(Map<String, LabelValue> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Sets tags.
     * @param tags A set of non-identifying attributes attached to this job.
     * @return this
     */
    public UpdateJobOptions setTags(Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Sets notes.
     * @param notes Notes attached to a job, sorted by timestamp.
     * @return this
     */
    public UpdateJobOptions setNotes(Map<String, String> notes) {
        this.notes = notes;
        return this;
    }
}
