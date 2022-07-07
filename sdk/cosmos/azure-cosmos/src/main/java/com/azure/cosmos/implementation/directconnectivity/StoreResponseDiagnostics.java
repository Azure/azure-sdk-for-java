// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents diagnostics from store response OR from cosmos exception
 */
public class StoreResponseDiagnostics {
    final static Logger logger = LoggerFactory.getLogger(StoreResponseDiagnostics.class);
    private final String partitionKeyRangeId;
    private final String sessionTokenAsString;
    private final double requestCharge;
    private final String activityId;
    private final String correlatedActivityId;
    private final int statusCode;
    private final int subStatusCode;
    private final int pendingRequestQueueSize;
    private final int requestPayloadLength;
    private final int responsePayloadLength;
    private final RequestTimeline requestTimeline;
    private final RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;
    private final int rntbdChannelTaskQueueSize;
    private final RntbdEndpointStatistics rntbdEndpointStatistics;
    private final int rntbdRequestLength;
    private final int rntbdResponseLength;
    private final String exceptionMessage;
    private final String exceptionResponseHeaders;
    private final Uri.HealthStatus uriHealthStatus;

    public static StoreResponseDiagnostics createStoreResponseDiagnostics(StoreResponse storeResponse) {
        return new StoreResponseDiagnostics(storeResponse);
    }

    public static StoreResponseDiagnostics createStoreResponseDiagnostics(CosmosException cosmosException) {
        return new StoreResponseDiagnostics(cosmosException);
    }

    private StoreResponseDiagnostics(StoreResponse storeResponse) {
        this.partitionKeyRangeId = storeResponse.getPartitionKeyRangeId();
        this.activityId = storeResponse.getActivityId();
        this.correlatedActivityId = storeResponse.getCorrelatedActivityId();
        this.requestCharge = storeResponse.getRequestCharge();
        this.sessionTokenAsString = storeResponse.getSessionTokenString();
        this.statusCode = storeResponse.getStatus();
        this.subStatusCode = storeResponse.getSubStatusCode();
        this.pendingRequestQueueSize = storeResponse.getPendingRequestQueueSize();
        this.requestPayloadLength = storeResponse.getRequestPayloadLength();
        this.responsePayloadLength = storeResponse.getResponseBodyLength();
        this.requestTimeline = storeResponse.getRequestTimeline();
        this.channelAcquisitionTimeline = storeResponse.getChannelAcquisitionTimeline();
        this.rntbdChannelTaskQueueSize = storeResponse.getRntbdChannelTaskQueueSize();
        this.rntbdEndpointStatistics = storeResponse.getEndpointStatistics();
        this.rntbdRequestLength = storeResponse.getRntbdRequestLength();
        this.rntbdResponseLength = storeResponse.getRntbdResponseLength();
        this.exceptionMessage = null;
        this.exceptionResponseHeaders = null;
        this.uriHealthStatus = storeResponse.getUriHealthStatus();
    }

    private StoreResponseDiagnostics(CosmosException e) {
        this.partitionKeyRangeId = BridgeInternal.getPartitionKeyRangeId(e);
        this.activityId = e.getActivityId();
        this.correlatedActivityId = e.getResponseHeaders().get(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID);;
        this.requestCharge = e.getRequestCharge();
        this.sessionTokenAsString = e.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        this.statusCode = e.getStatusCode();
        this.subStatusCode = e.getSubStatusCode();
        this.pendingRequestQueueSize = BridgeInternal.getRntbdPendingRequestQueueSize(e);
        this.requestPayloadLength = BridgeInternal.getRequestBodyLength(e);
        this.responsePayloadLength = BridgeInternal.getRntbdResponseLength(e);
        this.requestTimeline = BridgeInternal.getRequestTimeline(e);
        this.channelAcquisitionTimeline = BridgeInternal.getChannelAcqusitionTimeline(e);
        this.rntbdChannelTaskQueueSize = BridgeInternal.getChannelTaskQueueSize(e);
        this.rntbdEndpointStatistics = BridgeInternal.getServiceEndpointStatistics(e);
        this.rntbdRequestLength = BridgeInternal.getRntbdRequestLength(e);
        this.rntbdResponseLength = BridgeInternal.getRntbdResponseLength(e);
        this.exceptionMessage = BridgeInternal.getInnerErrorMessage(e);
        this.exceptionResponseHeaders = e.getResponseHeaders() != null ? e.getResponseHeaders().toString() : null;
        this.uriHealthStatus = ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor().getUriHealthStatus(e);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getSubStatusCode() {
        return subStatusCode;
    }

    public int getPendingRequestQueueSize() {
        return pendingRequestQueueSize;
    }

    public int getRequestPayloadLength() {
        return requestPayloadLength;
    }

    public int getResponsePayloadLength() {
        return responsePayloadLength;
    }

    public RequestTimeline getRequestTimeline() {
        return requestTimeline;
    }

    public RntbdChannelAcquisitionTimeline getChannelAcquisitionTimeline() {
        return channelAcquisitionTimeline;
    }

    public int getRntbdChannelTaskQueueSize() {
        return rntbdChannelTaskQueueSize;
    }

    public RntbdEndpointStatistics getRntbdEndpointStatistics() {
        return rntbdEndpointStatistics;
    }

    public int getRntbdRequestLength() {
        return rntbdRequestLength;
    }

    public int getRntbdResponseLength() {
        return rntbdResponseLength;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getPartitionKeyRangeId() {
        return partitionKeyRangeId;
    }

    public String getSessionTokenAsString() {
        return sessionTokenAsString;
    }

    public double getRequestCharge() {
        return requestCharge;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getCorrelatedActivityId() {
        return correlatedActivityId;
    }

    public String getExceptionResponseHeaders() {
        return exceptionResponseHeaders;
    }

    public String getUriHealthStatus() { return this.uriHealthStatus.toString(); }
}
