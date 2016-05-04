/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import org.joda.time.DateTime;
import com.microsoft.rest.DateTimeRfc1123;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Additional parameters for the ListPoolUsageMetrics operation.
 */
public class PoolListPoolUsageMetricsOptionsInner {
    /**
     * The earliest time from which to include metrics. This must be at least
     * two and a half hours before the current time.
     */
    @JsonProperty(value = "")
    private DateTime startTime;

    /**
     * The latest time from which to include metrics. This must be at least
     * two hours before the current time.
     */
    @JsonProperty(value = "")
    private DateTime endTime;

    /**
     * Sets an OData $filter clause.
     */
    @JsonProperty(value = "")
    private String filter;

    /**
     * Sets the maximum number of items to return in the response.
     */
    @JsonProperty(value = "")
    private Integer maxResults;

    /**
     * Sets the maximum time that the server can spend processing the request,
     * in seconds. The default is 30 seconds.
     */
    @JsonProperty(value = "")
    private Integer timeout;

    /**
     * Caller generated request identity, in the form of a GUID with no
     * decoration such as curly braces e.g.
     * 9C4D50EE-2D56-4CD3-8152-34347DC9F2B0.
     */
    @JsonProperty(value = "")
    private String clientRequestId;

    /**
     * Specifies if the server should return the client-request-id identifier
     * in the response.
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
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the filter value.
     *
     * @return the filter value
     */
    public String filter() {
        return this.filter;
    }

    /**
     * Set the filter value.
     *
     * @param filter the filter value to set
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Get the maxResults value.
     *
     * @return the maxResults value
     */
    public Integer maxResults() {
        return this.maxResults;
    }

    /**
     * Set the maxResults value.
     *
     * @param maxResults the maxResults value to set
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

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
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setTimeout(Integer timeout) {
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
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setClientRequestId(String clientRequestId) {
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
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setReturnClientRequestId(Boolean returnClientRequestId) {
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
     * @return the PoolListPoolUsageMetricsOptionsInner object itself.
     */
    public PoolListPoolUsageMetricsOptionsInner setOcpDate(DateTime ocpDate) {
        this.ocpDate = new DateTimeRfc1123(ocpDate);
        return this;
    }

}
