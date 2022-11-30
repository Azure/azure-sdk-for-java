// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * StoreResultDiagnostics is a combination of diagnostics from StoreResult, StoreResponse and CosmosException.
 *
 * This is just a model class for StoreResult Diagnostics. It doesn't contain any references to the actual store result, store response and cosmos exception.
 * We intend to keep it this way - decoupled with store result, store response and cosmos exception.
 *
 */
public class StoreResultDiagnostics {
    private final static Logger logger = LoggerFactory.getLogger(StoreResultDiagnostics.class);

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
            ? String.format("%s_%d", storePhysicalAddress.getURI().getHost(), storePhysicalAddress.getURI().getPort())
            : null;
    }

    public String getStorePhysicalAddressEscapedPath() {
        return storePhysicalAddress != null
            ? storePhysicalAddress.getURI().getPath()
            : null;
    }

    public boolean isThroughputControlRequestRateTooLargeException() {
        return isThroughputControlRequestRateTooLargeException;
    }

    public Double getBackendLatencyInMs() {
        return backendLatencyInMs;
    }

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
            this.writeNonNullStringField(jsonGenerator, "exceptionMessage", storeResponseDiagnostics.getExceptionMessage());
            this.writeNonNullStringField(jsonGenerator, "exceptionResponseHeaders", storeResponseDiagnostics.getExceptionResponseHeaders());
            this.writeNonNullObjectField(jsonGenerator, "replicaStatusList", storeResponseDiagnostics.getReplicaStatusList());
            jsonGenerator.writeObjectField("transportRequestTimeline", storeResponseDiagnostics.getRequestTimeline());

            this.writeNonNullObjectField(jsonGenerator,"transportRequestChannelAcquisitionContext", storeResponseDiagnostics.getChannelAcquisitionTimeline());

            jsonGenerator.writeObjectField("rntbdRequestLengthInBytes", storeResponseDiagnostics.getRntbdRequestLength());
            jsonGenerator.writeObjectField("rntbdResponseLengthInBytes", storeResponseDiagnostics.getRntbdResponseLength());
            jsonGenerator.writeObjectField("requestPayloadLengthInBytes", storeResponseDiagnostics.getRequestPayloadLength());
            jsonGenerator.writeObjectField("responsePayloadLengthInBytes", storeResponseDiagnostics.getResponsePayloadLength());
            jsonGenerator.writeObjectField("channelTaskQueueSize", storeResponseDiagnostics.getRntbdChannelTaskQueueSize());
            jsonGenerator.writeObjectField("pendingRequestsCount", storeResponseDiagnostics.getPendingRequestQueueSize());
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
    }
}
