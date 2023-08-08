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
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class PartitionKeyRangeIsSplittingException extends CosmosException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Partition key range is splitting exception.
     */
    public PartitionKeyRangeIsSplittingException() {
        this(RMResources.Gone);
    }

    /**
     * Instantiates a new Partition key range is splitting exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public PartitionKeyRangeIsSplittingException(CosmosError cosmosError, long lsn, String partitionKeyRangeId,
                                                 Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.GONE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
    }

    PartitionKeyRangeIsSplittingException(String msg) {
        super(HttpConstants.StatusCodes.GONE, msg);
        setSubStatus();
    }

    PartitionKeyRangeIsSplittingException(String msg, String resourceAddress) {
        super(msg, null, null, HttpConstants.StatusCodes.GONE, resourceAddress);
        setSubStatus();
    }

    /**
     * Instantiates a new Partition key range is splitting exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUri the request uri
     */
    public PartitionKeyRangeIsSplittingException(String message, HttpHeaders headers, String requestUri) {
        this(message, null, headers, requestUri);
    }

    PartitionKeyRangeIsSplittingException(Exception innerException) {
        this(RMResources.Gone, innerException, null, null);
    }

    PartitionKeyRangeIsSplittingException(String message,
                                          Exception innerException,
                                          HttpHeaders headers,
                                          String requestUri) {
        super(String.format("%s: %s", RMResources.Gone, message),
            innerException,
            HttpUtils.asMap(headers),
            HttpConstants.StatusCodes.GONE,
            requestUri);

        setSubStatus();
    }

    private void setSubStatus() {
        this.getResponseHeaders().put(
            WFConstants.BackendHeaders.SUB_STATUS,
            Integer.toString(HttpConstants.SubStatusCodes.COMPLETING_SPLIT_OR_MERGE));
    }
}
