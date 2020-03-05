// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosResponseDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeline;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Used internally to represents a response from the store.
 */
public class StoreResponse {
    final static Logger LOGGER = LoggerFactory.getLogger(StoreResponse.class);
    final private int status;
    final private Map<String, String> responseHeaders;
    final private byte[] content;

    private CosmosResponseDiagnostics cosmosResponseDiagnostics;
    private RequestTimeline requestTimeline;

    public StoreResponse(
            int status,
            Map<String, String> headerEntries,
            byte[] content) {

        requestTimeline = RequestTimeline.empty();
        responseHeaders = headerEntries;

        this.status = status;
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getResponseHeaders() {
        return this.responseHeaders;
    }

    public byte[] getResponseBody() {
        return this.content;
    }

    public long getLSN() {
        String lsnString = this.getHeaderValue(WFConstants.BackendHeaders.LSN);
        if (StringUtils.isNotEmpty(lsnString)) {
            return Long.parseLong(lsnString);
        }

        return -1;
    }

    public String getPartitionKeyRangeId() {
        return this.getHeaderValue(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID);
    }

    public String getContinuation() {
        return this.getHeaderValue(HttpConstants.HttpHeaders.CONTINUATION);
    }

    public boolean containsHeader(String key) {
        return this.responseHeaders != null && this.responseHeaders.containsKey(key);
    }

    public String setHeaderValue(String key, String value) {
        return this.responseHeaders.put(key, value);
    }

    public String getHeaderValue(String attribute) {
        return this.responseHeaders.get(attribute);
    }

    public CosmosResponseDiagnostics getCosmosResponseDiagnostics() {
        return cosmosResponseDiagnostics;
    }

    StoreResponse setCosmosResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
        return this;
    }

    void setRequestTimeline(RequestTimeline requestTimeline) {
        this.requestTimeline = requestTimeline;
    }

    RequestTimeline getRequestTimeline() {
        return this.requestTimeline;
    }

    int getSubStatusCode() {
        int subStatusCode = HttpConstants.SubStatusCodes.UNKNOWN;
        String subStatusCodeString = this.getHeaderValue(WFConstants.BackendHeaders.SUB_STATUS);
        if (StringUtils.isNotEmpty(subStatusCodeString)) {
            try {
                subStatusCode = Integer.parseInt(subStatusCodeString);
            } catch (NumberFormatException e) {
                // If value cannot be parsed as Integer, return Unknown.
            }
        }
        return subStatusCode;
    }
}
