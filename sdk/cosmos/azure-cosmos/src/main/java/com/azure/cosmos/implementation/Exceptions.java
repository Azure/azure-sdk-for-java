// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Exceptions {

    public static boolean isStatusCode(CosmosException e, int status) {
        return status == e.getStatusCode();
    }

    public static boolean isSubStatusCode(CosmosException e, int subStatus) {
        return subStatus == e.getSubStatusCode();
    }

    public static boolean isPartitionSplit(CosmosException e) {
        return isStatusCode(e, HttpConstants.StatusCodes.GONE)
                && isSubStatusCode(e, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);
    }

    public static boolean isNameCacheStale(CosmosException e) {
        return isStatusCode(e, HttpConstants.StatusCodes.GONE)
                && isSubStatusCode(e, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
    }
}
