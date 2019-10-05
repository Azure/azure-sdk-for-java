// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * Partitioning version.
 */
public enum PartitionKeyDefinitionVersion {

    /**
     * Original version of hash partitioning.
     */
    V1(1),

    /**
     * Enhanced version of hash partitioning - offers better distribution of long partition keys and uses less storage.
     *
     * This version should be used for any practical purpose, but it is available in newer SDKs only.
     */
    V2(2);

    int val;

    PartitionKeyDefinitionVersion(int val) {
        this.val = val;
    }

}
