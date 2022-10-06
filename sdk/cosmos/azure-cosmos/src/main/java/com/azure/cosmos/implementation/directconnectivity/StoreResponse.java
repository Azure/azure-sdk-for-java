// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used internally to represents a response from the store.
 */
public class StoreResponse {
    final static Logger LOGGER = LoggerFactory.getLogger(StoreResponse.class);
    final private int status;
    final private String[] responseHeaderNames;
    final private String[] responseHeaderValues;
    final private byte[] content;

    private int pendingRequestQueueSize;
    private int requestPayloadLength;
    private int responsePayloadLength;
    private RequestTimeline requestTimeline;
    private RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;
    private int rntbdChannelTaskQueueSize;
    private RntbdEndpointStatistics rntbdEndpointStatistics;
    private int rntbdRequestLength;
    private int rntbdResponseLength;
    private final List<String> replicaStatusList;

    public StoreResponse(
            int status,
            Map<String, String> headerMap,
            byte[] content) {

        requestTimeline = RequestTimeline.empty();
        responseHeaderNames = new String[headerMap.size()];
        responseHeaderValues = new String[headerMap.size()];

        int i = 0;
        for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
            responseHeaderNames[i] = headerEntry.getKey();
            responseHeaderValues[i] = headerEntry.getValue();
            i++;
        }

        this.status = status;
        this.content = content;
        if (this.content != null) {
            this.responsePayloadLength = this.content.length;
        }

        replicaStatusList = new ArrayList<>();
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
        return this.responsePayloadLength;
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

    public String getActivityId() {
        return this.getHeaderValue(HttpConstants.HttpHeaders.ACTIVITY_ID);
    }

    public String getCorrelatedActivityId() {
        return this.getHeaderValue(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID);
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

    public double getRequestCharge() {
        String value = this.getHeaderValue(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    public String getSessionTokenString() {
        return this.getHeaderValue(HttpConstants.HttpHeaders.SESSION_TOKEN);
    }

    public void setRequestTimeline(RequestTimeline requestTimeline) {
        this.requestTimeline = requestTimeline;
    }

    RequestTimeline getRequestTimeline() {
        return this.requestTimeline;
    }

    public void setChannelAcquisitionTimeline(RntbdChannelAcquisitionTimeline channelAcquisitionTimeline) {
        this.channelAcquisitionTimeline = channelAcquisitionTimeline;
    }

    RntbdChannelAcquisitionTimeline getChannelAcquisitionTimeline() {
        return this.channelAcquisitionTimeline;
    }

    public void setEndpointStatistics(RntbdEndpointStatistics rntbdEndpointStatistics) {
        this.rntbdEndpointStatistics = rntbdEndpointStatistics;
    }

    RntbdEndpointStatistics getEndpointStatistics() {
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

    public List<String> getReplicaStatusList() {
        return this.replicaStatusList;
    }
}
