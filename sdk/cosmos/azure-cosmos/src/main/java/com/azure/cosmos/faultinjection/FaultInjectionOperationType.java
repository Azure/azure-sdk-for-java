// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

/***
 * Fault injection operation type.
 */
public enum FaultInjectionOperationType {
    /**
     * Read items.
     */
    READ_DATA,
    /**
     * Query items.
     */
    QUERY_DATA,
    /**
     * Create item.
     */
    CREATE_DATA,
    /**
     * Upsert item.
     */
    UPSERT_DATA,
    /**
     * Replace item.
     */
    REPLACE_DATA,
    /**
     * Delete item.
     */
    DELETE_DATA,
    /**
     * Patch item.
     */
    PATCH_DATA

    // Add support for metadata request type
}
