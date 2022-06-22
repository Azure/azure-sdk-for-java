package com.azure.communication.jobrouter.implementation.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public abstract class JobOptions {
    protected String id;
    protected String channelReference;
    protected JobStatus jobStatus;
    protected OffsetDateTime enqueueTimeUtc;
    protected String channelId;
    protected String classificationPolicyId;
    protected String queueId;
    protected Integer priority;
    protected String dispositionCode;
    protected List<WorkerSelector> requestedWorkerSelectors;
    protected List<WorkerSelector> attachedWorkerSelectors;
    protected Map<String, Object> labels;
    protected Map<String, JobAssignment> assignments;
    protected Map<String, Object> tags;
    protected Map<String, String> notes;

    /*
     * Reference to an external parent context, eg. call ID.
     */
    public String getChannelReference() {
        return this.channelReference;
    }

    /*
     * The state of the Job.
     */
    public JobStatus getJobStatus() {
        return this.jobStatus;
    }

    /*
     * The time a job was queued.
     */
    public OffsetDateTime getEnqueueTimeUtc() {
        return this.enqueueTimeUtc;
    }

    /*
     * The channel identifier. eg. voice, chat, etc.
     */
    public String getChannelId() {
        return this.channelId;
    }

    /*
     * The Id of the Classification policy used for classifying a job.
     */
    public String getClassificationPolicyId() {
        return this.classificationPolicyId;
    }

    /*
     * The Id of the Queue that this job is queued to.
     */
    public String getQueueId() {
        return this.queueId;
    }

    /*
     * The priority of this job.
     */
    public Integer getPriority() {
        return this.priority;
    }

    /*
     * Reason code for cancelled or closed jobs.
     */
    public String getDispositionCode() {
        return this.dispositionCode;
    }

    /*
     * A collection of manually specified label selectors, which a worker must
     * satisfy in order to process this job.
     */
    public List<WorkerSelector> getRequestedWorkerSelectors() {
        return this.requestedWorkerSelectors;
    }

    /*
     * A collection of label selectors attached by a classification policy,
     * which a worker must satisfy in order to process this job.
     */
    public List<WorkerSelector> getAttachedWorkerSelectors() {
        return this.attachedWorkerSelectors;
    }

    /*
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    public Map<String, Object> getLabels() {
        return this.labels;
    }

    /*
     * A collection of the assignments of the job.
     * Key is AssignmentId.
     */
    public Map<String, JobAssignment> getAssignments() {
        return this.assignments;
    }

    /*
     * A set of non-identifying attributes attached to this job
     */
    public Map<String, Object> getTags() {
        return this.tags;
    }

    /*
     * Notes attached to a job, sorted by timestamp
     */
    public Map<String, String> getNotes() {
        return this.notes;
    }
}
