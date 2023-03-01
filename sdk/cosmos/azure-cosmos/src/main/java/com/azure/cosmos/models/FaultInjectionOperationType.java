// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

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
    DELETE

    // Add support for metadata request type
}
