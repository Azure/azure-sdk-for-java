// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

/***
 * Fault injection operation type.
 */
public enum FaultInjectionOperationType {
    /**
     * Read items.
     */
    READ_ITEM,
    /**
     * Query items.
     */
    QUERY_ITEM,
    /**
     * Create item.
     */
    CREATE_ITEM,
    /**
     * Upsert item.
     */
    UPSERT_ITEM,
    /**
     * Replace item.
     */
    REPLACE_ITEM,
    /**
     * Delete item.
     */
    DELETE_ITEM,
    /**
     * Patch item.
     */
    PATCH_ITEM

    // Add support for metadata request type
}
