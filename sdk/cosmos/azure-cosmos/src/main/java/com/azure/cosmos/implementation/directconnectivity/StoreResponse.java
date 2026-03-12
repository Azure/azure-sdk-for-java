// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelStatistics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBufInputStream;
import io.netty.util.IllegalReferenceCountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Used internally to represents a response from the store.
 */
public class StoreResponse {
    private static final Logger logger = LoggerFactory.getLogger(StoreResponse.class.getSimpleName());
    final private int status;
    final private Map<String, String> responseHeaders;
    private int requestPayloadLength;
    private RequestTimeline requestTimeline;
    private RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;
    private RntbdEndpointStatistics rntbdEndpointStatistics;
    private RntbdChannelStatistics channelStatistics;
    private int rntbdRequestLength;
    private int rntbdResponseLength;
    private final Map<String, Set<String>> replicaStatusList;
    private String faultInjectionRuleId;
    private List<String> faultInjectionRuleEvaluationResults;

    private final JsonNodeStorePayload responsePayload;
    private final String endpoint;

    public StoreResponse(
            String endpoint,
            int status,
            Map<String, String> headerMap,
            ByteBufInputStream contentStream,
            int responsePayloadLength) {

        checkArgument((contentStream == null) == (responsePayloadLength == 0),
            "Parameter 'contentStream' must be consistent with 'responsePayloadLength'.");
        requestTimeline = RequestTimeline.empty();
        this.responseHeaders = headerMap;
        this.endpoint = endpoint != null ? endpoint : "";

        this.status = status;
        replicaStatusList = new HashMap<>();
        if (contentStream != null) {
            try {
                this.responsePayload = new JsonNodeStorePayload(contentStream, responsePayloadLength, headerMap);
            } finally {
                try {
                    contentStream.close();
                } catch (Throwable e) {
                    if (!(e instanceof IllegalReferenceCountException)) {
                        // Log as warning instead of debug to make ByteBuf leak issues more visible
                        logger.warn("Failed to close content stream. This may cause a Netty ByteBuf leak.", e);
                    }
                }
            }
        } else {
            this.responsePayload = null;
        }
    }

    private StoreResponse(
        String endpoint,
        int status,
        Map<String, String> headerMap,
        JsonNodeStorePayload responsePayload) {

        checkNotNull(endpoint, "Parameter 'endpoint' must not be null.");

        requestTimeline = RequestTimeline.empty();
        this.responseHeaders = headerMap;
        this.endpoint = endpoint;

        this.status = status;
        replicaStatusList = new HashMap<>();
        this.responsePayload = responsePayload;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * @deprecated Use {@link #getResponseHeaders()} instead. This method creates a new array on every call.
     */
    @Deprecated
    public String[] getResponseHeaderNames() {
        return responseHeaders.keySet().toArray(new String[0]);
    }

    /**
     * @deprecated Use {@link #getResponseHeaders()} instead. This method creates a new array on every call.
     */
    @Deprecated
    public String[] getResponseHeaderValues() {
        return responseHeaders.values().toArray(new String[0]);
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

    public JsonNode getResponseBodyAsJson() {
        if (this.responsePayload == null) {
            return null;
        }

        return this.responsePayload.getPayload();
    }

    public int getResponseBodyLength() {
        if (this.responsePayload == null) {
            return 0;
        }

        return this.responsePayload.getResponsePayloadSize();
    }

    public long getLSN() {
        String lsnString = this.getHeaderValue(WFConstants.BackendHeaders.LSN);
        if (StringUtils.isNotEmpty(lsnString)) {
            return Long.parseLong(lsnString);
        }

        return -1;
    }

    // NOTE: Only used in local test through transport client interceptor
    public void setGCLSN(long gclsn) {
        this.setHeaderValue(WFConstants.BackendHeaders.GLOBAL_COMMITTED_LSN, String.valueOf(gclsn));
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
        if (this.responseHeaders == null) {
            return null;
        }

        // Headers are stored with lowercase keys
        String value = responseHeaders.get(attribute);
        if (value != null) {
            return value;
        }
        // Fallback to case-insensitive scan for backward compatibility
        for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(attribute)) {
                return entry.getValue();
            }
        }
        return null;
    }

    void setHeaderValue(String headerName, String value) {
        if (this.responseHeaders == null) {
            return;
        }

        // Try exact match first (lowercase keys)
        if (responseHeaders.containsKey(headerName)) {
            responseHeaders.put(headerName, value);
            return;
        }
        // Fallback to case-insensitive scan
        for (String key : responseHeaders.keySet()) {
            if (key.equalsIgnoreCase(headerName)) {
                responseHeaders.put(key, value);
                return;
            }
        }
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

    public void setChannelStatistics(RntbdChannelStatistics channelStatistics) {
        this.channelStatistics = channelStatistics;
    }

    public RntbdChannelStatistics getChannelStatistics() {
        return this.channelStatistics;
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

    public long getNumberOfReadRegions() {
        long numberOfReadRegions = -1L;
        String numberOfReadRegionsString = this.getHeaderValue(WFConstants.BackendHeaders.NUMBER_OF_READ_REGIONS);
        if (StringUtils.isNotEmpty(numberOfReadRegionsString)) {
            try {
                return Long.parseLong(numberOfReadRegionsString);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse NUMBER_OF_READ_REGIONS header value: {}. Returning -1.", numberOfReadRegionsString);
            }
        }
        return numberOfReadRegions;
    }

    public Map<String, Set<String>> getReplicaStatusList() {
        return this.replicaStatusList;
    }

    public String getFaultInjectionRuleId() {
        return this.faultInjectionRuleId;
    }

    public void setFaultInjectionRuleId(String faultInjectionRuleId) {
        this.faultInjectionRuleId = faultInjectionRuleId;
    }

    public List<String> getFaultInjectionRuleEvaluationResults() {
        return this.faultInjectionRuleEvaluationResults;
    }

    public void setFaultInjectionRuleEvaluationResults(List<String> results) {
        this.faultInjectionRuleEvaluationResults = results;
    }

    public StoreResponse withRemappedStatusCode(int newStatusCode, double additionalRequestCharge) {

        Map<String, String> headers = new HashMap<>(this.responseHeaders);
        // Update request charge
        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase(HttpConstants.HttpHeaders.REQUEST_CHARGE)) {
                double currentRequestCharge = this.getRequestCharge();
                double newRequestCharge = currentRequestCharge + additionalRequestCharge;
                headers.put(key, String.valueOf(newRequestCharge));
                break;
            }
        }

        return new StoreResponse(
            this.endpoint,
            newStatusCode,
            headers,
            this.responsePayload);
    }

    public String getEndpoint() {
        return this.endpoint;
    }
}
