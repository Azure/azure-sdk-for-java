/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The JobIdInner model.
 */
public class JobIdInner {
    /**
     * Id of the created job.
     */
    @JsonProperty(value = "JobId")
    private String jobId;

    /**
     * Get the jobId value.
     *
     * @return the jobId value
     */
    public String jobId() {
        return this.jobId;
    }

    /**
     * Set the jobId value.
     *
     * @param jobId the jobId value to set
     * @return the JobIdInner object itself.
     */
    public JobIdInner withJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

}
