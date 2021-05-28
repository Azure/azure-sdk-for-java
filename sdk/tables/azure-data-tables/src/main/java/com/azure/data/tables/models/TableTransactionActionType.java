// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.data.tables.TableClient;

/**
 * The type of action to be executed on a {@link TableEntity} in a transactional batch operation.
 */
public enum TableTransactionActionType {
    /**
     * Add the entity to the table. This is equivalent to {@link TableClient#createEntity(TableEntity)}.
     */
    CREATE,

    /**
     * Upsert the entity in {@link TableEntityUpdateMode#MERGE} mode. This is equivalent to
     * {@link TableClient#upsertEntity(TableEntity)}.
     */
    UPSERT_MERGE,

    /**
     * Upsert the entity in {@link TableEntityUpdateMode#REPLACE} mode. This is equivalent to
     * {@link TableClient#upsertEntity(TableEntity)}.
     */
    UPSERT_REPLACE,

    /**
     * Update the entity in {@link TableEntityUpdateMode#MERGE} mode. This is equivalent to
     * {@link TableClient#updateEntity(TableEntity)}.
     */
    UPDATE_MERGE,

    /**
     * Update the entity in {@link TableEntityUpdateMode#REPLACE} mode. This is equivalent to
     * {@link TableClient#updateEntity(TableEntity)}.
     */
    UPDATE_REPLACE,

    /**
     * Delete the entity. This is equivalent to {@link TableClient#deleteEntity(TableEntity)}.
     */
    DELETE
}
