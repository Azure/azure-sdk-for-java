/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.microsoft.rest.DateTimeRfc1123;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Additional parameters for the Task_Update operation.
 */
public class TaskUpdateOptions {
    /**
     * The maximum time that the server can spend processing the request, in
     * seconds. The default is 30 seconds.
     */
    @JsonProperty(value = "")
    private Integer timeout;

    /**
     * The caller-generated request identity, in the form of a GUID with no
     * decoration such as curly braces, e.g.
     * 9C4D50EE-2D56-4CD3-8152-34347DC9F2B0.
     */
    @JsonProperty(value = "")
    private String clientRequestId;

    /**
     * Whether the server should return the client-request-id identifier in
     * the response.
     */
    @JsonProperty(value = "")
    private Boolean returnClientRequestId;

    /**
     * The time the request was issued. If not specified, this header will be
     * automatically populated with the current system clock time.
     */
    @JsonProperty(value = "")
    private DateTimeRfc1123 ocpDate;

    /**
     * An ETag is specified. Specify this header to perform the operation only
     * if the resource's ETag is an exact match as specified.
     */
    @JsonProperty(value = "")
    private String ifMatch;

    /**
     * An ETag is specified. Specify this header to perform the operation only
     * if the resource's ETag does not match the specified ETag.
     */
    @JsonProperty(value = "")
    private String ifNoneMatch;

    /**
     * Specify this header to perform the operation only if the resource has
     * been modified since the specified date/time.
     */
    @JsonProperty(value = "")
    private DateTimeRfc1123 ifModifiedSince;

    /**
     * Specify this header to perform the operation only if the resource has
     * not been modified since the specified date/time.
     */
    @JsonProperty(value = "")
    private DateTimeRfc1123 ifUnmodifiedSince;

    /**
     * Get the timeout value.
     *
     * @return the timeout value
     */
    public Integer timeout() {
        return this.timeout;
    }

    /**
     * Set the timeout value.
     *
     * @param timeout the timeout value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Get the clientRequestId value.
     *
     * @return the clientRequestId value
     */
    public String clientRequestId() {
        return this.clientRequestId;
    }

    /**
     * Set the clientRequestId value.
     *
     * @param clientRequestId the clientRequestId value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
        return this;
    }

    /**
     * Get the returnClientRequestId value.
     *
     * @return the returnClientRequestId value
     */
    public Boolean returnClientRequestId() {
        return this.returnClientRequestId;
    }

    /**
     * Set the returnClientRequestId value.
     *
     * @param returnClientRequestId the returnClientRequestId value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withReturnClientRequestId(Boolean returnClientRequestId) {
        this.returnClientRequestId = returnClientRequestId;
        return this;
    }

    /**
     * Get the ocpDate value.
     *
     * @return the ocpDate value
     */
    public DateTime ocpDate() {
        if (this.ocpDate == null) {
            return null;
        }
        return this.ocpDate.getDateTime();
    }

    /**
     * Set the ocpDate value.
     *
     * @param ocpDate the ocpDate value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withOcpDate(DateTime ocpDate) {
        this.ocpDate = new DateTimeRfc1123(ocpDate);
        return this;
    }

    /**
     * Get the ifMatch value.
     *
     * @return the ifMatch value
     */
    public String ifMatch() {
        return this.ifMatch;
    }

    /**
     * Set the ifMatch value.
     *
     * @param ifMatch the ifMatch value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    /**
     * Get the ifNoneMatch value.
     *
     * @return the ifNoneMatch value
     */
    public String ifNoneMatch() {
        return this.ifNoneMatch;
    }

    /**
     * Set the ifNoneMatch value.
     *
     * @param ifNoneMatch the ifNoneMatch value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }

    /**
     * Get the ifModifiedSince value.
     *
     * @return the ifModifiedSince value
     */
    public DateTime ifModifiedSince() {
        if (this.ifModifiedSince == null) {
            return null;
        }
        return this.ifModifiedSince.getDateTime();
    }

    /**
     * Set the ifModifiedSince value.
     *
     * @param ifModifiedSince the ifModifiedSince value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withIfModifiedSince(DateTime ifModifiedSince) {
        this.ifModifiedSince = new DateTimeRfc1123(ifModifiedSince);
        return this;
    }

    /**
     * Get the ifUnmodifiedSince value.
     *
     * @return the ifUnmodifiedSince value
     */
    public DateTime ifUnmodifiedSince() {
        if (this.ifUnmodifiedSince == null) {
            return null;
        }
        return this.ifUnmodifiedSince.getDateTime();
    }

    /**
     * Set the ifUnmodifiedSince value.
     *
     * @param ifUnmodifiedSince the ifUnmodifiedSince value to set
     * @return the TaskUpdateOptions object itself.
     */
    public TaskUpdateOptions withIfUnmodifiedSince(DateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = new DateTimeRfc1123(ifUnmodifiedSince);
        return this;
    }

}
