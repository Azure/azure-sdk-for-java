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
    PATCH_ITEM,
    /**
     * Read container.
     */
    METADATA_REQUEST_CONTAINER,
    /**
     * Read database account.
     */
    METADATA_REQUEST_DATABASE_ACCOUNT,
    /**
     * Query query plan.
     */
    METADATA_REQUEST_QUERY_PLAN,
    /**
     * Refresh server addresses.
     */
    METADATA_REQUEST_REFRESH_ADDRESSES
}
