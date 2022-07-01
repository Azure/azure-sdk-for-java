// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.WorkerSelector;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public abstract class JobOptions {
    protected String id;
    protected String channelReference;
    protected OffsetDateTime enqueueTimeUtc;
    protected String channelId;
    protected String classificationPolicyId;
    protected String queueId;
    protected Integer priority;
    protected String dispositionCode;
    protected List<WorkerSelector> requestedWorkerSelectors;
    protected Map<String, Object> labels;
    protected Map<String, Object> tags;
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
     * Returns the time a job was queued.
     * @return enqueueTimeUtc
     */
    public OffsetDateTime getEnqueueTimeUtc() {
        return this.enqueueTimeUtc;
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
    public Map<String, Object> getLabels() {
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
