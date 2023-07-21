// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

/**
 * Request options to complete a job.
 * Job: A unit of work to be routed.
 */
public class CompleteJobOptions {
    /*
     * The id of the job to complete.
     */
    private final String jobId;

    /*
     * The assignment within the job to complete.
     */
    private final String assignmentId;

    /*
     * (Optional) A note that will be appended to the jobs' Notes collection with the current timestamp.
     */
    private String note;

    /**
     *  Creates an instance of CompleteJobOptions class.
     * @param jobId The id of job to complete.
     * @param assignmentId The assignment within the job to complete.
     */
    public CompleteJobOptions(String jobId, String assignmentId) {
        this.jobId = jobId;
        this.assignmentId = assignmentId;
    }

    /**
     * Get the jobId property: The id of the job to complete.
     *
     * @return the jobId value.
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Get the assignmentId property: The assignment within the job to complete.
     *
     * @return the assignmentId value.
     */
    public String getAssignmentId() {
        return this.assignmentId;
    }

    /**
     * Get the note property: (Optional) A note that will be appended to the jobs' Notes collection with the current
     * timestamp.
     *
     * @return the note value.
     */
    public String getNote() {
        return this.note;
    }

    /**
     * Set the note property: (Optional) A note that will be appended to the jobs' Notes collection with the current
     * timestamp.
     *
     * @param note the note value to set.
     * @return the CompleteJobOptions object itself.
     */
    public CompleteJobOptions setNote(String note) {
        this.note = note;
        return this;
    }
}
