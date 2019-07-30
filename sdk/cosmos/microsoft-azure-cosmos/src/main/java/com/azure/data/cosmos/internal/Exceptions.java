// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.CosmosClientException;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Exceptions {

    public static boolean isStatusCode(CosmosClientException e, int status) {
        return status == e.statusCode();
    }

    public static boolean isSubStatusCode(CosmosClientException e, int subStatus) {
        return subStatus == e.subStatusCode();
    }

    public static boolean isPartitionSplit(CosmosClientException e) {
        return isStatusCode(e, HttpConstants.StatusCodes.GONE)
                && isSubStatusCode(e, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);
    }

    public static boolean isNameCacheStale(CosmosClientException e) {
        return isStatusCode(e, HttpConstants.StatusCodes.GONE)
                && isSubStatusCode(e, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
    }
}
