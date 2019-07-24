// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.directconnectivity.WFConstants;
import com.azure.data.cosmos.internal.http.HttpHeaders;

import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class PartitionKeyRangeGoneException extends CosmosClientException {

    public PartitionKeyRangeGoneException() {
        this(RMResources.Gone);
    }

    public PartitionKeyRangeGoneException(CosmosError cosmosError, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.GONE, cosmosError, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
        this.setSubstatus();
    }

    public PartitionKeyRangeGoneException(String message) {
        this(message, null, null, null);
    }

    PartitionKeyRangeGoneException(String message, Exception innerException) {
        this(message, innerException, null, null);
    }

    PartitionKeyRangeGoneException(Exception innerException) {
        this(RMResources.Gone, innerException, null, null);
    }


    public PartitionKeyRangeGoneException(String message, HttpHeaders headers, String requestUriString) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUriString);
        this.setSubstatus();
    }

    PartitionKeyRangeGoneException(String message, Exception innerException, HttpHeaders headers, String requestUriString) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUriString);
        this.setSubstatus();
    }

    private void setSubstatus() {
        this.responseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE));
    }
}
