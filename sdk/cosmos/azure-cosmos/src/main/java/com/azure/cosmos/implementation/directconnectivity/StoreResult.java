// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ISessionToken;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RequestChargeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreResult {
    private final static Logger logger = LoggerFactory.getLogger(StoreResult.class);

    private final StoreResponse storeResponse;
    private final CosmosException exception;

    final public long lsn;
    final public String partitionKeyRangeId;
    final public long quorumAckedLSN;
    final public long globalCommittedLSN;
    final public long numberOfReadRegions;
    final public long itemLSN;
    final public ISessionToken sessionToken;
    final public double requestCharge;
    final public String activityId;
    final public String correlatedActivityId;
    final public int currentReplicaSetSize;
    final public int currentWriteQuorum;
    final public boolean isValid;
    final public boolean isGoneException;
    final public boolean isNotFoundException;
    final public boolean isInvalidPartitionException;
    final public Uri storePhysicalAddress;
    final public boolean isThroughputControlRequestRateTooLargeException;
    final public Double backendLatencyInMs;

    public StoreResult(
            StoreResponse storeResponse,
            CosmosException exception,
            String partitionKeyRangeId,
            long lsn,
            long quorumAckedLsn,
            double requestCharge,
            String activityId,
            String correlatedActivityId,
            int currentReplicaSetSize,
            int currentWriteQuorum,
            boolean isValid,
            Uri storePhysicalAddress,
            long globalCommittedLSN,
            int numberOfReadRegions,
            long itemLSN,
            ISessionToken sessionToken,
            Double backendLatencyInMs) {
        this.storeResponse = storeResponse;
        this.exception = exception;
        this.partitionKeyRangeId = partitionKeyRangeId;
        this.lsn = lsn;
        this.quorumAckedLSN = quorumAckedLsn;
        this.requestCharge = requestCharge;
        this.activityId= activityId;
        this.correlatedActivityId = correlatedActivityId;
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
        this.isThroughputControlRequestRateTooLargeException = this.exception != null && Exceptions.isThroughputControlRequestRateTooLargeException(this.exception);
        this.backendLatencyInMs = backendLatencyInMs;
    }

    public StoreResponse getStoreResponse() {
        return storeResponse;
    }

    public CosmosException getException() throws InternalServerErrorException {
        if (this.exception == null) {
            String message = "Exception should be available but found none";
            assert false : message;
            logger.error(message);
            throw new InternalServerErrorException(RMResources.InternalServerError);
        }

        return exception;
    }

    public StoreResponse toResponse() {
        return toResponse(null);
    }

    public StoreResponse toResponse(RequestChargeTracker requestChargeTracker) {
        if (!this.isValid) {
            if (this.exception == null) {
                logger.error("Exception not set for invalid response");
                throw new InternalServerErrorException(RMResources.InternalServerError);
            }

            throw this.exception;
        }

        if (requestChargeTracker != null) {
            StoreResult.setRequestCharge(this.storeResponse, this.exception, requestChargeTracker.getTotalRequestCharge());
        }

        if (this.exception != null) {
            throw exception;
        }

        return this.storeResponse;
    }

    private static void setRequestCharge(StoreResponse response, CosmosException cosmosException, double totalRequestCharge) {
        String totalRequestChargeString = Double.toString(totalRequestCharge);
        if (cosmosException != null) {
            cosmosException.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_CHARGE, totalRequestChargeString);
        } else {
            // Set total charge as final charge for the response.
            response.getResponseHeaders().put(HttpConstants.HttpHeaders.REQUEST_CHARGE, totalRequestChargeString);
        }
    }
}
