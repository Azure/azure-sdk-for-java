package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.JobAssignment;
import com.azure.communication.jobrouter.implementation.models.JobOptions;
import com.azure.communication.jobrouter.implementation.models.JobStatus;
import com.azure.communication.jobrouter.implementation.models.WorkerSelector;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class CreateJobOptions extends JobOptions {
    public CreateJobOptions(String id, String channelReference, JobStatus jobStatus, String channelId, String queueId, Integer priority) {
        this.id = id;
        this.channelReference = channelReference;
        this.jobStatus = jobStatus;
        this.channelId = channelId;
        this.queueId = queueId;
        this.priority = priority;
    }

    public CreateJobOptions setEnqueueTimeUtc(OffsetDateTime enqueueTimeUtc) {
        this.enqueueTimeUtc = enqueueTimeUtc;
        return this;
    }

    public CreateJobOptions setClassificationPolicyId(String classificationPolicyId) {
        this.classificationPolicyId = classificationPolicyId;
        return this;
    }

    public CreateJobOptions setDispositionCode(String dispositionCode) {
        this.dispositionCode = dispositionCode;
        return this;
    }

    public CreateJobOptions setRequestedWorkerSelectors(List<WorkerSelector> requestedWorkerSelectors) {
        this.requestedWorkerSelectors = requestedWorkerSelectors;
        return this;
    }

    public CreateJobOptions setAttachedWorkerSelectors(List<WorkerSelector> attachedWorkerSelectors) {
        this.attachedWorkerSelectors = attachedWorkerSelectors;
        return this;
    }

    public CreateJobOptions setLabels(Map<String, Object> labels) {
        this.labels = labels;
        return this;
    }

    public CreateJobOptions setAssignments(Map<String, JobAssignment> assignments) {
        this.assignments = assignments;
        return this;
    }

    public CreateJobOptions setTags(Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    public CreateJobOptions setNotes(Map<String, String> notes) {
        this.notes = notes;
        return this;
    }
}
