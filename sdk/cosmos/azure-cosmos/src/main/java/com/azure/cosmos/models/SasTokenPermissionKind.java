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
    CONTAINER_CREATE_STORE_PROCEDURES,
    CONTAINER_READ_STORE_PROCEDURES,
    CONTAINER_REPLACE_STORE_PROCEDURES,
    CONTAINER_DELETE_STORE_PROCEDURES,
    CONTAINER_CREATE_TRIGGERS,
    CONTAINER_READ_TRIGGERS,
    CONTAINER_REPLACE_TRIGGERS,
    CONTAINER_DELETE_TRIGGERS,
    CONTAINER_CREATE_USER_DEFINED_FUNCTIONS,
    CONTAINER_READ_USER_DEFINED_FUNCTIONS,
    CONTAINER_REPLACE_USER_DEFINED_FUNCTIONS,
    CONTAINER_DELETE_USER_DEFINED_FUNCTIONS,
    CONTAINER_EXECUTE_STORED_PROCEDURES,
    CONTAINER_READ_CONFLICTS,
    CONTAINER_DELETE_CONFLICTS,
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
    STORE_PROCEDURE_READ,
    STORE_PROCEDURE_REPLACE,
    STORE_PROCEDURE_DELETE,
    STORE_PROCEDURE_EXECUTE,

    /**
     * Cosmos DB User Defined Function scope.
     */
    USER_DEFINED_FUNCTION_READ,
    USER_DEFINED_FUNCTION_REPLACE,
    USER_DEFINED_FUNCTION_DELETE,

    /**
     * Cosmos DB Trigger scope.
     */
    TRIGGER_READ,
    TRIGGER_REPLACE,
    TRIGGER_DELETE,
}
