// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.exception;

import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.http.HttpHeaders;

import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class CosmosPartitionKeyRangeGoneException extends PartitionKeyRangeGoneException {

    /**
     * Instantiates a new Partition key range gone exception.
     */
    public CosmosPartitionKeyRangeGoneException() {
        super();
    }

    /**
     * Instantiates a new Partition key range gone exception.
     *
     * @param cosmosError the cosmos error
     * @param lsn the lsn
     * @param partitionKeyRangeId the partition key range id
     * @param responseHeaders the response headers
     */
    public CosmosPartitionKeyRangeGoneException(CosmosError cosmosError,
                                          long lsn,
                                          String partitionKeyRangeId,
                                          Map<String, String> responseHeaders) {
        super(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
    }

    /**
     * Instantiates a new Partition key range gone exception.
     *
     * @param message the message
     */
    public CosmosPartitionKeyRangeGoneException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Partition key range gone exception.
     *
     * @param message the message
     * @param headers the headers
     * @param requestUriString the request uri string
     */
    public CosmosPartitionKeyRangeGoneException(String message, HttpHeaders headers, String requestUriString) {
        super(message, headers, requestUriString);
    }

}
