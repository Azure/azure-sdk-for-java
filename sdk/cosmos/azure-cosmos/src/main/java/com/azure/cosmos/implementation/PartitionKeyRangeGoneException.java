// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class PartitionKeyRangeGoneException extends CosmosException {

    /**
     * Instantiates a new Partition key range gone exception.
     */
    public PartitionKeyRangeGoneException() {
        this(RMResources.Gone);
    }

    /**
     * Instantiates a new Partition key range gone exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public PartitionKeyRangeGoneException(CosmosError cosmosError,
                                          long lsn,
                                          String partitionKeyRangeId,
                                          Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.GONE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
        this.setSubstatus();
    }

    /**
     * Instantiates a new Partition key range gone exception.
     *
     * @param message the message
     */
    public PartitionKeyRangeGoneException(String message) {
        this(message, null, null, null);
    }

    PartitionKeyRangeGoneException(String message, Exception innerException) {
        this(message, innerException, null, null);
    }

    PartitionKeyRangeGoneException(Exception innerException) {
        this(RMResources.Gone, innerException, null, null);
    }


    /**
     * Instantiates a new Partition key range gone exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public PartitionKeyRangeGoneException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUriString);
        this.setSubstatus();
    }

    PartitionKeyRangeGoneException(String message, Exception innerException, HttpHeaders headers,
                                   String requestUriString) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUriString);
        this.setSubstatus();
    }

    private void setSubstatus() {
        this.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS,
            Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE));
    }
}
