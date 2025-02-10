// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelAcquisitionTimeline;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdChannelStatistics;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpointStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final int requestPayloadLength;
    private final int responsePayloadLength;
    private final RequestTimeline requestTimeline;
    private final RntbdChannelAcquisitionTimeline channelAcquisitionTimeline;
    private final RntbdEndpointStatistics rntbdEndpointStatistics;
    private final RntbdChannelStatistics rntbdChannelStatistics;
    private final int rntbdRequestLength;
    private final int rntbdResponseLength;
    private final String exceptionMessage;
    private final String exceptionResponseHeaders;
    private final Map<String, Set<String>> replicaStatusList;
    private final String faultInjectionRuleId;
    private final List<String> faultInjectionEvaluationResults;

    public static StoreResponseDiagnostics createStoreResponseDiagnostics(
        StoreResponse storeResponse,
        RxDocumentServiceRequest rxDocumentServiceRequest) {

        return new StoreResponseDiagnostics(storeResponse, rxDocumentServiceRequest);
    }

    public static StoreResponseDiagnostics createStoreResponseDiagnostics(
        CosmosException cosmosException,
        RxDocumentServiceRequest rxDocumentServiceRequest) {

        return new StoreResponseDiagnostics(cosmosException, rxDocumentServiceRequest);
    }

    private StoreResponseDiagnostics(StoreResponse storeResponse, RxDocumentServiceRequest rxDocumentServiceRequest) {
        String rspPkRangeId = storeResponse.getPartitionKeyRangeId();
        this.partitionKeyRangeId = !Strings.isNullOrWhiteSpace(rspPkRangeId) ? rspPkRangeId :
            rxDocumentServiceRequest.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
        this.activityId = storeResponse.getActivityId();
        this.correlatedActivityId = storeResponse.getCorrelatedActivityId();
        this.requestCharge = storeResponse.getRequestCharge();
        this.sessionTokenAsString = storeResponse.getSessionTokenString();
        this.statusCode = storeResponse.getStatus();
        this.subStatusCode = storeResponse.getSubStatusCode();
        this.requestPayloadLength = storeResponse.getRequestPayloadLength();
        this.responsePayloadLength = storeResponse.getResponseBodyLength();
        this.requestTimeline = storeResponse.getRequestTimeline();
        this.channelAcquisitionTimeline = storeResponse.getChannelAcquisitionTimeline();
        this.rntbdEndpointStatistics = storeResponse.getEndpointStatistics();
        this.rntbdChannelStatistics  = storeResponse.getChannelStatistics();
        this.rntbdRequestLength = storeResponse.getRntbdRequestLength();
        this.rntbdResponseLength = storeResponse.getRntbdResponseLength();
        this.exceptionMessage = null;
        this.exceptionResponseHeaders = null;
        this.replicaStatusList = storeResponse.getReplicaStatusList();
        this.faultInjectionRuleId = storeResponse.getFaultInjectionRuleId();
        this.faultInjectionEvaluationResults = storeResponse.getFaultInjectionRuleEvaluationResults();
    }

    private StoreResponseDiagnostics(CosmosException e, RxDocumentServiceRequest rxDocumentServiceRequest) {
        String rspPkRangeId = BridgeInternal.getPartitionKeyRangeId(e);
        this.partitionKeyRangeId = !Strings.isNullOrWhiteSpace(rspPkRangeId) ? rspPkRangeId :
            rxDocumentServiceRequest.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
        this.activityId = e.getActivityId();
        this.correlatedActivityId = e.getResponseHeaders().get(HttpConstants.HttpHeaders.CORRELATED_ACTIVITY_ID);;
        this.requestCharge = e.getRequestCharge();
        this.sessionTokenAsString = e.getResponseHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN);
        this.statusCode = e.getStatusCode();
        this.subStatusCode = e.getSubStatusCode();
        this.requestPayloadLength = BridgeInternal.getRequestBodyLength(e);
        this.responsePayloadLength = BridgeInternal.getRntbdResponseLength(e);
        this.requestTimeline = BridgeInternal.getRequestTimeline(e);
        this.channelAcquisitionTimeline = BridgeInternal.getChannelAcqusitionTimeline(e);
        this.rntbdEndpointStatistics = BridgeInternal.getServiceEndpointStatistics(e);
        this.rntbdChannelStatistics =
            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .getRntbdChannelStatistics(e);
        this.rntbdRequestLength = BridgeInternal.getRntbdRequestLength(e);
        this.rntbdResponseLength = BridgeInternal.getRntbdResponseLength(e);
        this.exceptionMessage = BridgeInternal.getInnerErrorMessage(e);
        this.exceptionResponseHeaders = e.getResponseHeaders() != null ? e.getResponseHeaders().toString() : null;
        this.replicaStatusList = ImplementationBridgeHelpers.CosmosExceptionHelper.getCosmosExceptionAccessor().getReplicaStatusList(e);
        this.faultInjectionRuleId =
            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .getFaultInjectionRuleId(e);
        this.faultInjectionEvaluationResults =
            ImplementationBridgeHelpers
                .CosmosExceptionHelper
                .getCosmosExceptionAccessor()
                .getFaultInjectionEvaluationResults(e);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public int getSubStatusCode() {
        return subStatusCode;
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

    public RntbdEndpointStatistics getRntbdEndpointStatistics() {
        return rntbdEndpointStatistics;
    }

    public RntbdChannelStatistics getRntbdChannelStatistics() {
        return this.rntbdChannelStatistics;
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

    public String getFaultInjectionRuleId() {
        return this.faultInjectionRuleId;
    }

    public List<String> getFaultInjectionEvaluationResults() {
        return this.faultInjectionEvaluationResults;
    }

    public Map<String, Set<String>> getReplicaStatusList() { return this.replicaStatusList; }
}
