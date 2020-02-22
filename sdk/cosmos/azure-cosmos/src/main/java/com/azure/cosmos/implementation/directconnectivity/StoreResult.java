// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.InternalServerErrorException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RequestChargeTracker;
import com.azure.cosmos.implementation.Strings;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;

public class StoreResult {
    private final static Logger logger = LoggerFactory.getLogger(StoreResult.class);

    private final StoreResponse storeResponse;
    private final CosmosClientException exception;

    final public long lsn;
    final public String partitionKeyRangeId;
    final public long quorumAckedLSN;
    final public long globalCommittedLSN;
    final public long numberOfReadRegions;
    final public long itemLSN;
    final public ISessionToken sessionToken;
    final public double requestCharge;
    final public int currentReplicaSetSize;
    final public int currentWriteQuorum;
    final public boolean isValid;
    final public boolean isGoneException;
    final public boolean isNotFoundException;
    final public boolean isInvalidPartitionException;
    final public Uri storePhysicalAddress;

    public StoreResult(
            StoreResponse storeResponse,
            CosmosClientException exception,
            String partitionKeyRangeId,
            long lsn,
            long quorumAckedLsn,
            double requestCharge,
            int currentReplicaSetSize,
            int currentWriteQuorum,
            boolean isValid,
            Uri storePhysicalAddress,
            long globalCommittedLSN,
            int numberOfReadRegions,
            long itemLSN,
            ISessionToken sessionToken) {
        this.storeResponse = storeResponse;
        this.exception = exception;
        this.partitionKeyRangeId = partitionKeyRangeId;
        this.lsn = lsn;
        this.quorumAckedLSN = quorumAckedLsn;
        this.requestCharge = requestCharge;
        this.currentReplicaSetSize = currentReplicaSetSize;
        this.currentWriteQuorum = currentWriteQuorum;
        this.isValid = isValid;
        this.isGoneException = this.exception != null && this.exception.getStatusCode() == HttpConstants.StatusCodes.GONE;
        this.isNotFoundException = this.exception != null && this.exception.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND;
        this.isInvalidPartitionException = this.exception != null
                && Exceptions.isNameCacheStale(this.exception);
        this.storePhysicalAddress = storePhysicalAddress;
        this.globalCommittedLSN = globalCommittedLSN;
        this.numberOfReadRegions = numberOfReadRegions;
        this.itemLSN = itemLSN;
        this.sessionToken = sessionToken;
    }

    public CosmosClientException getException() throws InternalServerErrorException {
        if (this.exception == null) {
            String message = "Exception should be available but found none";
            assert false : message;
            logger.error(message);
            throw new InternalServerErrorException(RMResources.InternalServerError);
        }

        return exception;
    }

    public StoreResponse toResponse() throws CosmosClientException {
        return toResponse(null);
    }

    public StoreResponse toResponse(RequestChargeTracker requestChargeTracker) throws CosmosClientException {
        if (!this.isValid) {
            if (this.exception == null) {
                logger.error("Exception not set for invalid response");
                throw new InternalServerErrorException(RMResources.InternalServerError);
            }

            throw this.exception;
        }

        if (requestChargeTracker != null && this.isValid) {
            StoreResult.setRequestCharge(this.storeResponse, this.exception, requestChargeTracker.getTotalRequestCharge());
        }

        if (this.exception != null) {
            throw exception;
        }

        return this.storeResponse;
    }

    private static void setRequestCharge(StoreResponse response, CosmosClientException cosmosClientException, double totalRequestCharge) {
        if (cosmosClientException != null) {
            cosmosClientException.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_CHARGE,
                    Double.toString(totalRequestCharge));
        }
        // Set total charge as final charge for the response.
        else if (response.getResponseHeaderNames() != null) {
            for (int i = 0; i < response.getResponseHeaderNames().length; ++i) {
                if (Strings.areEqualIgnoreCase(
                        response.getResponseHeaderNames()[i],
                        HttpConstants.HttpHeaders.REQUEST_CHARGE)) {
                    response.getResponseHeaderValues()[i] = Double.toString(totalRequestCharge);
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        int statusCode = 0;
        int subStatusCode = HttpConstants.SubStatusCodes.UNKNOWN;

        if (this.storeResponse != null) {
            statusCode = this.storeResponse.getStatus();
            subStatusCode = this.storeResponse.getSubStatusCode();
        } else if (this.exception != null) {
            statusCode = this.exception.getStatusCode();
            subStatusCode = this.exception.getSubStatusCode();
        }

        return "storePhysicalAddress: " + this.storePhysicalAddress +
                ", lsn: " + this.lsn +
                ", globalCommittedLsn: " + this.globalCommittedLSN +
                ", partitionKeyRangeId: " + this.partitionKeyRangeId +
                ", isValid: " + this.isValid +
                ", statusCode: " + statusCode +
                ", subStatusCode: " + subStatusCode +
                ", isGone: " + this.isGoneException +
                ", isNotFound: " + this.isNotFoundException +
                ", isInvalidPartition: " + this.isInvalidPartitionException +
                ", requestCharge: " + this.requestCharge +
                ", itemLSN: " + this.itemLSN +
                ", sessionToken: " + (this.sessionToken != null ? this.sessionToken.convertToString() : null) +
                ", exception: " + BridgeInternal.getInnerErrorMessage(this.exception);
    }
    public static class StoreResultSerializer extends StdSerializer<StoreResult> {

        public StoreResultSerializer(){
            super(StoreResult.class);
        }

        @Override
        public void serialize(StoreResult storeResult,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            int statusCode = 0;
            int subStatusCode = HttpConstants.SubStatusCodes.UNKNOWN;

            if (storeResult.storeResponse != null) {
                statusCode = storeResult.storeResponse.getStatus();
                subStatusCode = storeResult.storeResponse.getSubStatusCode();
            } else if (storeResult.exception != null) {
                statusCode = storeResult.exception.getStatusCode();
                subStatusCode = storeResult.exception.getSubStatusCode();
            }
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("storePhysicalAddress", storeResult.storePhysicalAddress);
            jsonGenerator.writeNumberField("lsn", storeResult.lsn);
            jsonGenerator.writeNumberField("globalCommittedLsn", storeResult.globalCommittedLSN);
            jsonGenerator.writeStringField("partitionKeyRangeId", storeResult.partitionKeyRangeId);
            jsonGenerator.writeBooleanField("isValid", storeResult.isValid);
            jsonGenerator.writeNumberField("statusCode", statusCode);
            jsonGenerator.writeNumberField("subStatusCode", subStatusCode);
            jsonGenerator.writeBooleanField("isGone", storeResult.isGoneException);
            jsonGenerator.writeBooleanField("isNotFound", storeResult.isNotFoundException);
            jsonGenerator.writeBooleanField("isInvalidPartition", storeResult.isInvalidPartitionException);
            jsonGenerator.writeNumberField("requestCharge", storeResult.requestCharge);
            jsonGenerator.writeNumberField("itemLSN", storeResult.itemLSN);
            jsonGenerator.writeStringField("sessionToken", (storeResult.sessionToken != null ? storeResult.sessionToken.convertToString() : null));
            jsonGenerator.writeStringField("exception", BridgeInternal.getInnerErrorMessage(storeResult.exception));
            jsonGenerator.writeObjectField("transportRequestTimeline", storeResult.storeResponse != null ? storeResult.storeResponse.getRequestTimeline() : null);
            jsonGenerator.writeEndObject();
        }
    }
}
