// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.util.List;
import java.util.Map;

/**
 * Abstract class for Create and Update JobOptions.
 */
public abstract class JobOptions {
    /**
     * The id of the job.
     */
    protected String id;

    /**
     * Reference to an external parent context, eg. call ID.
     */
    protected String channelReference;

    /**
     * The channel identifier. eg. voice, chat, etc.
     */
    protected String channelId;

    /**
     * The Id of the Classification policy used for classifying a job.
     */
    protected String classificationPolicyId;

    /**
     * The Id of the Queue that this job is queued to.
     */
    protected String queueId;

    /**
     * The priority of this job.
     */
    protected Integer priority;

    /**
     * Reason code for cancelled or closed jobs.
     */
    protected String dispositionCode;

    /**
     * A collection of manually specified label selectors, which a worker must
     * satisfy in order to process this job.
     */
    protected List<WorkerSelector> requestedWorkerSelectors;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    protected Map<String, LabelValue> labels;

    /**
     * A set of non-identifying attributes attached to this job.
     */
    protected Map<String, Object> tags;

    /**
     * Notes attached to a job, sorted by timestamp.
     */
    protected Map<String, String> notes;

    /**
     * Returns the id of RouterJob.
     * @return id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the reference to an external parent context, eg. call ID.
     * @return channelReference
     */
    public String getChannelReference() {
        return this.channelReference;
    }

    /**
     * Returns the channel identifier. eg. voice, chat, etc.
     * @return channelId
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * Returns the Id of the Classification policy used for classifying a job.
     * @return classificationPolicyId
     */
    public String getClassificationPolicyId() {
        return this.classificationPolicyId;
    }

    /**
     * Returns the Id of the Queue that this job is queued to.
     * @return queueId
     */
    public String getQueueId() {
        return this.queueId;
    }

    /**
     * Returns the priority of this job.
     * @return priority
     */
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * Returns the reason code for cancelled or closed jobs.
     * @return dispositionCode
     */
    public String getDispositionCode() {
        return this.dispositionCode;
    }

    /**
     * Returns the collection of manually specified label selectors that a worker must
     * satisfy in order to process a job.
     * @return requestedWorkerSelectors
     */
    public List<WorkerSelector> getRequestedWorkerSelectors() {
        return this.requestedWorkerSelectors;
    }

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     * @return labels
     */
    public Map<String, LabelValue> getLabels() {
        return this.labels;
    }

    /**
     * A set of non-identifying attributes attached to this job
     * @return tags
     */
    public Map<String, Object> getTags() {
        return this.tags;
    }

    /**
     * Notes attached to a job, sorted by timestamp
     * @return note
     */
    public Map<String, String> getNotes() {
        return this.notes;
    }
}
