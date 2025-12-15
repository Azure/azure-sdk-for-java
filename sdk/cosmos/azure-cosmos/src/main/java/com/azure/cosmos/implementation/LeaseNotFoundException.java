// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;

import java.util.Map;

public class LeaseNotFoundException extends CosmosException {

    /**
     * Instantiates a new LeaseNotFound exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public LeaseNotFoundException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                         Map<String, String> responseHeaders) {
        super(RMResources.LeaseNotFound, HttpConstants.StatusCodes.GONE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
        this.setSubStatus(HttpConstants.SubStatusCodes.LEASE_NOT_FOUND);
    }

    public LeaseNotFoundException(String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(RMResources.LeaseNotFound, HttpConstants.StatusCodes.GONE, null, responseHeaders);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
        this.setSubStatus(HttpConstants.SubStatusCodes.LEASE_NOT_FOUND);
    }

    private void setSubStatus(int subStatusCode) {
        this.getResponseHeaders().put(
            HttpConstants.HttpHeaders.SUB_STATUS,
            Integer.toString(subStatusCode));
    }
}
