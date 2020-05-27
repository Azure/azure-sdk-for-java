// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map.Entry;

/**
 * Used internally to represents a response from the store.
 */
public class StoreResponse {
    final static Logger LOGGER = LoggerFactory.getLogger(StoreResponse.class);
    final private int status;
    final private byte[] content;
    final HttpHeaders httpHeaders;

    private CosmosDiagnostics cosmosDiagnostics;
    private RequestTimeline requestTimeline;

    public StoreResponse(
            int status,
            HttpHeaders headerEntries,
            byte[] content) {
        httpHeaders = headerEntries;
        requestTimeline = RequestTimeline.empty();

        this.status = status;
        this.content = content;
    }

    public StoreResponse newStoreResponse(
        int status,
        List<Entry<String, String>> headerEntries,
        byte[] content) {

        HttpHeaders headersMap = new HttpHeaders();
        for(Entry<String, String> headerEntry: headerEntries) {
            headersMap.put( headerEntry.getKey(),  headerEntry.getValue());
        }

        return new StoreResponse(status, headersMap, content);
    }

    public int getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return  this.httpHeaders;
    }

    public byte[] getResponseBody() {
        return this.content;
    }

    public long getLSN() {
        String lsnString = this.httpHeaders.getValue(WFConstants.BackendHeaders.LSN);
        if (StringUtils.isNotEmpty(lsnString)) {
            return Long.parseLong(lsnString);
        }

        return -1;
    }

    public String getPartitionKeyRangeId() {
        return this.httpHeaders.getValue(WFConstants.BackendHeaders.PARTITION_KEY_RANGE_ID);
    }

    public String getContinuation() {
        return this.httpHeaders.getValue(HttpConstants.Headers.CONTINUATION);
    }

    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    StoreResponse setCosmosDiagnostics(CosmosDiagnostics cosmosDiagnostics) {
        this.cosmosDiagnostics = cosmosDiagnostics;
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
        String subStatusCodeString = this.httpHeaders.getValue(WFConstants.BackendHeaders.SUB_STATUS);
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
