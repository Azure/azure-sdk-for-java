// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * Specifies the set of access condition types that can be used for operations in the Azure Cosmos DB database service.
 */
public enum AccessConditionType {
    /**
     * Check if the resource's ETag value matches the ETag value performed.
     */
    IF_MATCH,

    /**
     * Check if the resource's ETag value does not match ETag value performed.
     */
    IF_NONE_MATCH
}
