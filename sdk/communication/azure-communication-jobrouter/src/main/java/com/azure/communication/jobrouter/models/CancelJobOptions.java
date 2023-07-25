// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

/**
 * Request options to cancel a job.
 * Job: A unit of work to be routed.
 */
public class CancelJobOptions {

    /**
     * Id of the job to cancel.
     */
    private final String jobId;

    /*
     * A note that will be appended to the jobs' Notes collection with the current timestamp.
     */
    private String note;

    /*
     * Indicates the outcome of the job, populate this field with your own custom values.
     * If not provided, default value of "Cancelled" is set.
     */
    private String dispositionCode;

    /**
     * Creates an instance of CancelJobRequest class.
     *
     * @param jobId Id of the job to cancel.
     */
    public CancelJobOptions(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Get the jobId property
     * @return the jobId value.
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Get the note property: A note that will be appended to the jobs' Notes collection with the current
     * timestamp.
     *
     * @return the note value.
     */
    public String getNote() {
        return this.note;
    }

    /**
     * Set the note property: A note that will be appended to the jobs' Notes collection with the current
     * timestamp.
     *
     * @param note the note value to set.
     * @return the CancelJobRequest object itself.
     */
    public CancelJobOptions setNote(String note) {
        this.note = note;
        return this;
    }

    /**
     * Get the dispositionCode property: Indicates the outcome of the job, populate this field with your own custom
     * values. If not provided, default value of "Cancelled" is set.
     *
     * @return the dispositionCode value.
     */
    public String getDispositionCode() {
        return this.dispositionCode;
    }

    /**
     * Set the dispositionCode property: Indicates the outcome of the job, populate this field with your own custom
     * values. If not provided, default value of "Cancelled" is set.
     *
     * @param dispositionCode the dispositionCode value to set.
     * @return the CancelJobRequest object itself.
     */
    public CancelJobOptions setDispositionCode(String dispositionCode) {
        this.dispositionCode = dispositionCode;
        return this;
    }
}
