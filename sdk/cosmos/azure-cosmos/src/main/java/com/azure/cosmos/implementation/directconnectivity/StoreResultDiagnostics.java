// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.ArrayUtils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

/**
 * StoreResultDiagnostics is a combination of diagnostics from StoreResult, StoreResponse and CosmosException.
 * This is just a model class for StoreResult Diagnostics. It doesn't contain any references to the actual store result, store response and cosmos exception.
 * We intend to keep it this way - decoupled with store result, store response and cosmos exception.
 *
 */
public class StoreResultDiagnostics {

    //  StoreResult fields
    private final long lsn;
    private final long quorumAckedLSN;
    private final long globalCommittedLSN;
    private final long numberOfReadRegions;
    private final long itemLSN;
    private final int currentReplicaSetSize;
    private final int currentWriteQuorum;
    private final boolean isValid;
    private boolean isGoneException;
    private boolean isNotFoundException;
    private boolean isInvalidPartitionException;
    private final Uri storePhysicalAddress;
    private boolean isThroughputControlRequestRateTooLargeException;
    private final Double backendLatencyInMs;
    private final Double retryAfterInMs;

    //  StoreResponse and CosmosException fields
    private StoreResponseDiagnostics storeResponseDiagnostics;

    public static StoreResultDiagnostics createStoreResultDiagnostics(
        StoreResult storeResult,
        RxDocumentServiceRequest request) {

        if (storeResult == null) {
            return null;
        } else if (storeResult.getStoreResponse() != null) {
            return new StoreResultDiagnostics(storeResult, storeResult.getStoreResponse(), request);
        } else {
            return new StoreResultDiagnostics(storeResult, storeResult.getException(), request);
        }
    }

    private StoreResultDiagnostics(StoreResult storeResult) {
        this.lsn = storeResult.lsn;
        this.quorumAckedLSN = storeResult.quorumAckedLSN;
        this.currentReplicaSetSize = storeResult.currentReplicaSetSize;
        this.currentWriteQuorum = storeResult.currentWriteQuorum;
        this.isValid = storeResult.isValid;
        this.storePhysicalAddress = storeResult.storePhysicalAddress != null ? storeResult.storePhysicalAddress : null;
        this.globalCommittedLSN = storeResult.globalCommittedLSN;
        this.numberOfReadRegions = storeResult.numberOfReadRegions;
        this.itemLSN = storeResult.itemLSN;
        this.backendLatencyInMs = storeResult.backendLatencyInMs;
        this.retryAfterInMs = storeResult.retryAfterInMs;
    }

    private StoreResultDiagnostics(StoreResult storeResult, CosmosException e, RxDocumentServiceRequest request) {
        this(storeResult);
        this.isGoneException = Exceptions.isGone(e);
        this.isNotFoundException = Exceptions.isNotFound(e);
        this.isInvalidPartitionException = Exceptions.isNameCacheStale(e);
        this.isThroughputControlRequestRateTooLargeException = Exceptions.isThroughputControlRequestRateTooLargeException(e);
        this.storeResponseDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(e, request);
    }

    private StoreResultDiagnostics(
        StoreResult storeResult,
        StoreResponse storeResponse,
        RxDocumentServiceRequest request) {

        this(storeResult);
        this.storeResponseDiagnostics = StoreResponseDiagnostics.createStoreResponseDiagnostics(storeResponse, request);
    }

    public long getLsn() {
        return lsn;
    }

    public long getQuorumAckedLSN() {
        return quorumAckedLSN;
    }

    public long getGlobalCommittedLSN() {
        return globalCommittedLSN;
    }

    public long getNumberOfReadRegions() {
        return numberOfReadRegions;
    }

    public long getItemLSN() {
        return itemLSN;
    }

    public int getCurrentReplicaSetSize() {
        return currentReplicaSetSize;
    }

    public int getCurrentWriteQuorum() {
        return currentWriteQuorum;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isGoneException() {
        return isGoneException;
    }

    public boolean isNotFoundException() {
        return isNotFoundException;
    }

    public boolean isInvalidPartitionException() {
        return isInvalidPartitionException;
    }

    public String getStorePhysicalAddressAsString() {
        return storePhysicalAddress != null ? storePhysicalAddress.getURIAsString() : null;
    }

    public String getStorePhysicalAddressEscapedAuthority() {
        return storePhysicalAddress != null
            ? storePhysicalAddress.getURI().getHost() + "_" +  storePhysicalAddress.getURI().getPort()
            : null;
    }

    public String getStorePhysicalAddressEscapedPath() {
        return storePhysicalAddress != null
            ? storePhysicalAddress.getURI().getPath()
            : null;
    }

    public String[] getPartitionAndReplicaId() {
        return getPartitionAndReplicaId(this.getStorePhysicalAddressEscapedPath());
    }

    public static String[] getPartitionAndReplicaId(String serviceAddress) {
        if (serviceAddress == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        String[] serviceAddressParts = serviceAddress.split("/");
        // Sample value for serviceAddress
        // /apps/f88bfdf4-2954-4324-aad3-f1686668076d/services/3359112a-719d-474e-aa51-e89a142ae1b3/partitions/512fe816-24fa-4fbb-bbb1-587d2ce19851/replicas/133038444008943156p/
        if (serviceAddressParts.length != 9) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] result = new String[2];
        result[0] = serviceAddressParts[6];
        result[1] = serviceAddressParts[8];

        return result;
    }

    public boolean isThroughputControlRequestRateTooLargeException() {
        return isThroughputControlRequestRateTooLargeException;
    }

    public Double getBackendLatencyInMs() {
        return backendLatencyInMs;
    }

    public Double getRetryAfterInMs() { return retryAfterInMs; }

    public StoreResponseDiagnostics getStoreResponseDiagnostics() {
        return storeResponseDiagnostics;
    }

    public static class StoreResultDiagnosticsSerializer extends StdSerializer<StoreResultDiagnostics> {
        private static final long serialVersionUID = 5315472126043077905L;

        public StoreResultDiagnosticsSerializer(){
            super(StoreResultDiagnostics.class);
        }

        @Override
        public void serialize(StoreResultDiagnostics storeResultDiagnostics,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            StoreResponseDiagnostics storeResponseDiagnostics = storeResultDiagnostics.getStoreResponseDiagnostics();
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(
                    "storePhysicalAddress",
                    storeResultDiagnostics.storePhysicalAddress == null ? null : storeResultDiagnostics.storePhysicalAddress.getURIAsString());
            jsonGenerator.writeNumberField("lsn", storeResultDiagnostics.lsn);
            jsonGenerator.writeNumberField("quorumAckedLSN",storeResultDiagnostics.quorumAckedLSN);
            jsonGenerator.writeNumberField("currentReplicaSetSize", storeResultDiagnostics.currentReplicaSetSize);
            jsonGenerator.writeNumberField("globalCommittedLsn", storeResultDiagnostics.globalCommittedLSN);
            jsonGenerator.writeStringField("partitionKeyRangeId", storeResponseDiagnostics.getPartitionKeyRangeId());
            jsonGenerator.writeBooleanField("isValid", storeResultDiagnostics.isValid);
            jsonGenerator.writeNumberField("statusCode", storeResponseDiagnostics.getStatusCode());
            jsonGenerator.writeNumberField("subStatusCode", storeResponseDiagnostics.getSubStatusCode());
            jsonGenerator.writeBooleanField("isGone", storeResultDiagnostics.isGoneException);
            jsonGenerator.writeBooleanField("isNotFound", storeResultDiagnostics.isNotFoundException);
            jsonGenerator.writeBooleanField("isInvalidPartition", storeResultDiagnostics.isInvalidPartitionException);
            jsonGenerator.writeBooleanField("isThroughputControlRequestRateTooLarge", storeResultDiagnostics.isThroughputControlRequestRateTooLargeException);
            jsonGenerator.writeNumberField("requestCharge", storeResponseDiagnostics.getRequestCharge());
            jsonGenerator.writeNumberField("itemLSN", storeResultDiagnostics.itemLSN);
            jsonGenerator.writeStringField("sessionToken", storeResponseDiagnostics.getSessionTokenAsString());
            jsonGenerator.writeObjectField("backendLatencyInMs", storeResultDiagnostics.backendLatencyInMs);
            jsonGenerator.writeObjectField("retryAfterInMs", storeResultDiagnostics.retryAfterInMs);
            this.writeNonNullStringField(jsonGenerator, "exceptionMessage", storeResponseDiagnostics.getExceptionMessage());
            this.writeNonNullStringField(jsonGenerator, "exceptionResponseHeaders", storeResponseDiagnostics.getExceptionResponseHeaders());
            this.writeNonNullStringField(jsonGenerator, "faultInjectionRuleId", storeResponseDiagnostics.getFaultInjectionRuleId());

            if (StringUtils.isEmpty(storeResponseDiagnostics.getFaultInjectionRuleId())) {
                this.writeNonEmptyStringArrayField(
                    jsonGenerator,
                    "faultInjectionEvaluationResults",
                    storeResponseDiagnostics.getFaultInjectionEvaluationResults());
            }

            this.writeNonNullObjectField(jsonGenerator, "replicaStatusList", storeResponseDiagnostics.getReplicaStatusList());
            jsonGenerator.writeObjectField("transportRequestTimeline", storeResponseDiagnostics.getRequestTimeline());

            this.writeNonNullObjectField(jsonGenerator,"transportRequestChannelAcquisitionContext", storeResponseDiagnostics.getChannelAcquisitionTimeline());

            jsonGenerator.writeObjectField("rntbdRequestLengthInBytes", storeResponseDiagnostics.getRntbdRequestLength());
            jsonGenerator.writeObjectField("rntbdResponseLengthInBytes", storeResponseDiagnostics.getRntbdResponseLength());
            jsonGenerator.writeObjectField("requestPayloadLengthInBytes", storeResponseDiagnostics.getRequestPayloadLength());
            jsonGenerator.writeObjectField("responsePayloadLengthInBytes", storeResponseDiagnostics.getResponsePayloadLength());
            jsonGenerator.writeObjectField("channelStatistics", storeResponseDiagnostics.getRntbdChannelStatistics());
            jsonGenerator.writeObjectField("serviceEndpointStatistics", storeResponseDiagnostics.getRntbdEndpointStatistics());

            jsonGenerator.writeEndObject();
        }

        private void writeNonNullObjectField(JsonGenerator jsonGenerator, String fieldName, Object object) throws IOException {
             if (object == null) {
                 return;
             }

             jsonGenerator.writeObjectField(fieldName, object);
        }

        private void writeNonNullStringField(JsonGenerator jsonGenerator, String fieldName, String value) throws IOException {
            if (value == null) {
                return;
            }

            jsonGenerator.writeStringField(fieldName, value);
        }

        private void writeNonEmptyStringArrayField(JsonGenerator jsonGenerator, String fieldName, List<String> values) throws IOException {
            if (values == null || values.isEmpty()) {
                return;
            }

            jsonGenerator.writeObjectField(fieldName, values);
        }
    }
}
