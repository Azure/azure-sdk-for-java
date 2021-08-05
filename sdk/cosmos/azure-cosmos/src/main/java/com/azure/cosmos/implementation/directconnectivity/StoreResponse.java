// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionContext;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
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
    final private String[] responseHeaderNames;
    final private String[] responseHeaderValues;
    final private byte[] content;

    private CosmosDiagnostics cosmosDiagnostics;
    private int pendingRequestQueueSize;
    private int requestPayloadLength;
    private RequestTimeline requestTimeline;
    private RntbdChannelAcquisitionContext channelAcquisitionContext;
    private int rntbdChannelTaskQueueSize;
    private RntbdEndpointStatistics rntbdEndpointStatistics;
    private int rntbdRequestLength;
    private int rntbdResponseLength;

    public StoreResponse(
            int status,
            List<Entry<String, String>> headerEntries,
            byte[] content) {

        requestTimeline = RequestTimeline.empty();
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

    public int getRntbdChannelTaskQueueSize() {
        return rntbdChannelTaskQueueSize;
    }

    public void setRntbdChannelTaskQueueSize(int rntbdChannelTaskQueueSize) {
        this.rntbdChannelTaskQueueSize = rntbdChannelTaskQueueSize;
    }

    public int getPendingRequestQueueSize() {
        return this.pendingRequestQueueSize;
    }

    public void setRntbdPendingRequestSize(int pendingRequestQueueSize) {
        this.pendingRequestQueueSize = pendingRequestQueueSize;
    }

    public void setRntbdRequestLength(int rntbdRequestLength) {
        this.rntbdRequestLength = rntbdRequestLength;
    }

    public void setRntbdResponseLength(int rntbdResponseLength) {
        this.rntbdResponseLength = rntbdResponseLength;
    }

    public int getRntbdRequestLength() {
        return rntbdRequestLength;
    }

    public int getRntbdResponseLength() {
        return rntbdResponseLength;
    }

    public int getRequestPayloadLength() {
        return requestPayloadLength;
    }

    public void setRequestPayloadLength(int requestPayloadLength) {
        this.requestPayloadLength = requestPayloadLength;
    }

    public byte[] getResponseBody() {
        return this.content;
    }

    public int getResponseBodyLength() {
        return (this.content != null) ? this.content.length : 0;
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

    public CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    public double getRequestCharge() {
        String value = this.getHeaderValue(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
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

    void setChannelAcquisitionContext(RntbdChannelAcquisitionContext channelAcquisitionContext) {
        this.channelAcquisitionContext = channelAcquisitionContext;
    }

    RntbdChannelAcquisitionContext getChannelAcquisitionContext() {
        return this.channelAcquisitionContext;
    }

    void setEndpointStatistics(RntbdEndpointStatistics rntbdEndpointStatistics) {
        this.rntbdEndpointStatistics = rntbdEndpointStatistics;
    }

    RntbdEndpointStatistics getEndpointStsts() {
        return this.rntbdEndpointStatistics;
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
