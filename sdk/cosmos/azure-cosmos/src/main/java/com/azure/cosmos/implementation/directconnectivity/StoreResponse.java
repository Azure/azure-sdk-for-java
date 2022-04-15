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

    private int pendingRequestQueueSize;
    private int requestPayloadLength;
    private int responsePayloadLength;
    private RequestTimeline requestTimeline;
    private RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;
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
        if (this.content != null) {
            this.responsePayloadLength = this.content.length;
        }
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

    public double getRequestCharge() {
        String value = this.getHeaderValue(HttpConstants.HttpHeaders.REQUEST_CHARGE);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    /**
     * Static factory method to create serializable store response to be used in CosmosDiagnostics
     * @param storeResponse store response
     * @return serializable store response
     */
    public static StoreResponse createSerializableStoreResponse(StoreResponse storeResponse) {
        if (storeResponse == null) {
            return null;
        }
        return new StoreResponse(storeResponse);
    }

    void setRequestTimeline(RequestTimeline requestTimeline) {
        this.requestTimeline = requestTimeline;
    }

    RequestTimeline getRequestTimeline() {
        return this.requestTimeline;
    }

    void setChannelAcquisitionTimeline(RntbdChannelAcquisitionTimeline channelAcquisitionTimeline) {
        this.channelAcquisitionTimeline = channelAcquisitionTimeline;
    }

    RntbdChannelAcquisitionTimeline getChannelAcquisitionTimeline() {
        return this.channelAcquisitionTimeline;
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

    /**
     * Private copy constructor, only to be used internally for serialization purposes
     * <p>
     * NOTE: This constructor does not copy all the fields to avoid memory issues.
     */
    private StoreResponse(StoreResponse storeResponse) {
        this.responseHeaderValues = null;
        this.responseHeaderNames = null;
        this.content = null;
        this.status = storeResponse.status;
        this.pendingRequestQueueSize = storeResponse.pendingRequestQueueSize;
        this.requestPayloadLength = storeResponse.requestPayloadLength;
        this.responsePayloadLength = storeResponse.responsePayloadLength;
        this.requestTimeline = storeResponse.requestTimeline;
        this.channelAcquisitionTimeline = storeResponse.channelAcquisitionTimeline;
        this.rntbdChannelTaskQueueSize = storeResponse.rntbdChannelTaskQueueSize;
        this.rntbdEndpointStatistics = storeResponse.rntbdEndpointStatistics;
        this.rntbdRequestLength = storeResponse.rntbdRequestLength;
        this.rntbdResponseLength = storeResponse.rntbdResponseLength;
    }
}
