// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

/**
 * The type of action to be executed on a {@link TableEntity} in a transactional operation.
 */
public enum TableTransactionActionType {
    /**
     * Add the entity to the table. This is equivalent to {@code TableClient.createEntity()} or
     * {@code TableAsyncClient.createEntity()}.
     */
    CREATE,

    /**
     * Upsert the entity in {@link TableEntityUpdateMode#MERGE} mode. This is equivalent to
     * {@code TableClient.upsertEntity()} or {@code TableAsyncClient.upsertEntity()}.
     */
    UPSERT_MERGE,

    /**
     * Upsert the entity in {@link TableEntityUpdateMode#REPLACE} mode. This is equivalent to
     * {@code TableClient.upsertEntity()} or {@code TableAsyncClient.upsertEntity()}.
     */
    UPSERT_REPLACE,

    /**
     * Update the entity in {@link TableEntityUpdateMode#MERGE} mode. This is equivalent to
     * {@code TableClient.updateEntity()} or {@code TableAsyncClient.updateEntity()}.
     */
    UPDATE_MERGE,

    /**
     * Update the entity in {@link TableEntityUpdateMode#REPLACE} mode. This is equivalent to
     * {@code TableClient.updateEntity()} or {@code TableAsyncClient.updateEntity()}.
     */
    UPDATE_REPLACE,

    /**
     * Delete the entity. This is equivalent to {@code TableClient.deleteEntity()} or
     * {@code TableAsyncClient.deleteEntity()}.
     */
    DELETE
}
