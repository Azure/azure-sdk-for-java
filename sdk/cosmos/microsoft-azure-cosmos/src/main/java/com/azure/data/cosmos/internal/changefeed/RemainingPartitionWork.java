// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed;

/**
 * Interface for remaining partition work.
 */
public interface RemainingPartitionWork {
    /**
     * @return the partition key range ID for which the remaining work is calculated.
     */
    String getPartitionKeyRangeId();

    /**
     * @return the ammount of documents remaining to be processed.
     */
    long getRemainingWork();
}
