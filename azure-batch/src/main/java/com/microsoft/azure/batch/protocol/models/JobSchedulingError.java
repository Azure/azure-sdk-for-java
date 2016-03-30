/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An error encountered by the Batch service when scheduling a job.
 */
public class JobSchedulingError {
    /**
     * Gets or sets the category of the job scheduling error. Possible values
     * include: 'usererror', 'servererror', 'unmapped'.
     */
    @JsonProperty(required = true)
    private SchedulingErrorCategory category;

    /**
     * Gets or sets an identifier for the job scheduling error.  Codes are
     * invariant and are intended to be consumed programmatically.
     */
    private String code;

    /**
     * Gets or sets a message describing the job scheduling error, intended to
     * be suitable for display in a user interface.
     */
    private String message;

    /**
     * Gets or sets a list of additional error details related to the
     * scheduling error.
     */
    private List<NameValuePair> details;

    /**
     * Get the category value.
     *
     * @return the category value
     */
    public SchedulingErrorCategory getCategory() {
        return this.category;
    }

    /**
     * Set the category value.
     *
     * @param category the category value to set
     */
    public void setCategory(SchedulingErrorCategory category) {
        this.category = category;
    }

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message value.
     *
     * @param message the message value to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the details value.
     *
     * @return the details value
     */
    public List<NameValuePair> getDetails() {
        return this.details;
    }

    /**
     * Set the details value.
     *
     * @param details the details value to set
     */
    public void setDetails(List<NameValuePair> details) {
        this.details = details;
    }

}
