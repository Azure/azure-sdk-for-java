// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.CosmosResponseDiagnostics;
import com.azure.data.cosmos.internal.HttpConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;

/**
 * Used internally to represents a response from the store.
 */
public class StoreResponse {
    final static Logger LOGGER = LoggerFactory.getLogger(StoreResponse.class);
    final private int status;
    final private String[] responseHeaderNames;
    final private String[] responseHeaderValues;
    final private InputStream httpEntityStream;
    final private String content;

    private CosmosResponseDiagnostics cosmosResponseDiagnostics;

    public StoreResponse(int status, List<Entry<String, String>> headerEntries, InputStream inputStream) {
        this(status, headerEntries, null, inputStream);
    }

    public StoreResponse(int status, List<Entry<String, String>> headerEntries, String content) {
        this(status, headerEntries, content, null);
    }

    private StoreResponse(
            int status,
            List<Entry<String, String>> headerEntries, 
            String content,
            InputStream inputStream) {
        responseHeaderNames = new String[headerEntries.size()];
        responseHeaderValues = new String[headerEntries.size()];

        int i = 0;

        for(Entry<String, String> headerEntry: headerEntries) {
            responseHeaderNames[i] = headerEntry.getKey();
            responseHeaderValues[i] = headerEntry.getValue();
            i++;
        }

        this.status = status;

        this.content = content;
        this.httpEntityStream = inputStream;
    }

    public int getStatus() {
        return status;
    }

    public String[] getResponseHeaderNames() {
        return responseHeaderNames;
    }

    public String[] getResponseHeaderValues() {
        return responseHeaderValues;
    }

    public String getResponseBody() {
        return this.content;
    }

    public InputStream getResponseStream() {
        // Some operation type doesn't have a response stream so this can be null
        return this.httpEntityStream;
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

    public String getHeaderValue(String attribute) {
        if (this.responseHeaderValues == null || this.responseHeaderNames.length != this.responseHeaderValues.length) {
            return null;
        }

        for (int i = 0; i < responseHeaderNames.length; i++) {
            if (responseHeaderNames[i].equalsIgnoreCase(attribute)) {
                return responseHeaderValues[i];
            }
        }

        return null;
    }

    public CosmosResponseDiagnostics getCosmosResponseDiagnostics() {
        return cosmosResponseDiagnostics;
    }

    void setCosmosResponseDiagnostics(CosmosResponseDiagnostics cosmosResponseDiagnostics) {
        this.cosmosResponseDiagnostics = cosmosResponseDiagnostics;
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
