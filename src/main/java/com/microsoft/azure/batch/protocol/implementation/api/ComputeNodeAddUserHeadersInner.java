/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.microsoft.rest.DateTimeRfc1123;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for AddUser operation.
 */
public class ComputeNodeAddUserHeadersInner {
    /**
     * Gets the ClientRequestId provided by the client during the request, if
     * present and requested to be returned.
     */
    @JsonProperty(value = "client-request-id")
    private String clientRequestId;

    /**
     * Gets the value that uniquely identifies a request.
     */
    @JsonProperty(value = "request-id")
    private String requestId;

    /**
     * Gets the content of the ETag HTTP response header.
     */
    @JsonProperty(value = "ETag")
    private String eTag;

    /**
     * Gets the content of the Last-Modified HTTP response header.
     */
    @JsonProperty(value = "Last-Modified")
    private DateTimeRfc1123 lastModified;

    /**
     * Gets the OData id of the resource to which the request applied.
     */
    @JsonProperty(value = "DataServiceId")
    private String dataServiceId;

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
     * @return the ComputeNodeAddUserHeadersInner object itself.
     */
    public ComputeNodeAddUserHeadersInner setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
        return this;
    }

    /**
     * Get the requestId value.
     *
     * @return the requestId value
     */
    public String requestId() {
        return this.requestId;
    }

    /**
     * Set the requestId value.
     *
     * @param requestId the requestId value to set
     * @return the ComputeNodeAddUserHeadersInner object itself.
     */
    public ComputeNodeAddUserHeadersInner setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     * @return the ComputeNodeAddUserHeadersInner object itself.
     */
    public ComputeNodeAddUserHeadersInner setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime lastModified() {
        if (this.lastModified == null) {
            return null;
        }
        return this.lastModified.getDateTime();
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     * @return the ComputeNodeAddUserHeadersInner object itself.
     */
    public ComputeNodeAddUserHeadersInner setLastModified(DateTime lastModified) {
        this.lastModified = new DateTimeRfc1123(lastModified);
        return this;
    }

    /**
     * Get the dataServiceId value.
     *
     * @return the dataServiceId value
     */
    public String dataServiceId() {
        return this.dataServiceId;
    }

    /**
     * Set the dataServiceId value.
     *
     * @param dataServiceId the dataServiceId value to set
     * @return the ComputeNodeAddUserHeadersInner object itself.
     */
    public ComputeNodeAddUserHeadersInner setDataServiceId(String dataServiceId) {
        this.dataServiceId = dataServiceId;
        return this;
    }

}
