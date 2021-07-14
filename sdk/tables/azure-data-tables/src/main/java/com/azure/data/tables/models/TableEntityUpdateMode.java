// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

/**
 * The type of update operation to perform on an existing entity within a table.
 */
public enum TableEntityUpdateMode {
    /**
     * The provided entity's properties will be merged into the existing entity.
     */
    MERGE,

    /**
     * The provided entity's properties will completely replace those in the existing entity.
     */
    REPLACE
}
