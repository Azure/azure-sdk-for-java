/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Data Lake Analytics job state audit records for tracking the lifecycle
 * of a job.
 */
public class JobStateAuditRecord {
    /**
     * the new state the job is in.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String newState;

    /**
     * the time stamp that the state change took place.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DateTime timeStamp;

    /**
     * the user who requests the change.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String requestedByUser;

    /**
     * the details of the audit log.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String details;

    /**
     * Get the newState value.
     *
     * @return the newState value
     */
    public String newState() {
        return this.newState;
    }

    /**
     * Get the timeStamp value.
     *
     * @return the timeStamp value
     */
    public DateTime timeStamp() {
        return this.timeStamp;
    }

    /**
     * Get the requestedByUser value.
     *
     * @return the requestedByUser value
     */
    public String requestedByUser() {
        return this.requestedByUser;
    }

    /**
     * Get the details value.
     *
     * @return the details value
     */
    public String details() {
        return this.details;
    }

}
