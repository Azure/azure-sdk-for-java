// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

/**
 * Type of Start and End key for ReadFeedKey
 */
public enum ReadFeedKeyType {
    /**
     *   Use resource name
     */
    ResourceId,

    /**
     * Use effective partition key
     */
    EffectivePartitionKey
}
