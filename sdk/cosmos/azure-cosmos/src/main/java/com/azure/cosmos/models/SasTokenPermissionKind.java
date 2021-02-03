// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Defines permission scopes applicable when generating a Cosmos DB shared access signature token.
 */
@Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum SasTokenPermissionKind {
    /**
     * Cosmos DB Container Resources scope.
     */
    CONTAINER_CREATE_ITEMS,
    CONTAINER_REPLACE_ITEMS,
    CONTAINER_UPSERT_ITEMS,
    CONTAINER_DELETE_ITEMS,
    CONTAINER_EXECUTE_QUERIES,
    CONTAINER_READ_FEEDS,
    CONTAINER_EXECUTE_STORED_PROCEDURES,
    CONTAINER_MANAGE_CONFLICTS,
    CONTAINER_READ_ANY,
    CONTAINER_FULL_ACCESS,

    /**
     * Cosmos DB Item scope.
     */
    ITEM_READ_ANY,
    ITEM_FULL_ACCESS,
    ITEM_READ,
    ITEM_REPLACE,
    ITEM_UPSERT,
    ITEM_DELETE,

    /**
     * Cosmos DB Store Procedure scope.
     */
    STORE_PROCEDURE_EXECUTE,
}
