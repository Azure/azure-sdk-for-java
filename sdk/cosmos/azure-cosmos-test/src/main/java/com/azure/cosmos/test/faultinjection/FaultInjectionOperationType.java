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
     * Batch item.
     */
    BATCH_ITEM,
    /**
     * Read container request.
     */
    METADATA_REQUEST_CONTAINER,
    /**
     * Read database account request.
     */
    METADATA_REQUEST_DATABASE_ACCOUNT,
    /**
     * Query query plan request.
     */
    METADATA_REQUEST_QUERY_PLAN,
    /**
     * Partition key ranges request.
     */
    METADATA_REQUEST_PARTITION_KEY_RANGES,
    /**
     * Address refresh request.
     */
    METADATA_REQUEST_ADDRESS_REFRESH,
    /**
     * Read change feed items
     */
    READ_FEED_ITEM,
    /**
     * Head collection request - barrier request for document operation
     */
    HEAD_COLLECTION
}
