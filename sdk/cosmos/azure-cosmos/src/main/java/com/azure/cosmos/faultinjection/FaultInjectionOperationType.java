// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

/***
 * Fault injection operation type.
 */
public enum FaultInjectionOperationType {
    /**
     * READ.
     */
    READ,
    /**
     * QUERY
     */
    QUERY,
    /**
     * CREATE
     */
    CREATE,
    /**
     * UPSERT
     */
    UPSERT,
    /**
     * REPLACE
     */
    REPLACE,
    /**
     * DELETE
     */
    DELETE,
    /**
     * PATCH
     */
    PATCH

    // Add support for metadata request type
}
