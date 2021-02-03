// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.sastokens;

import java.util.Locale;

import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_CREATE_ITEMS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_CREATE_STORED_PROCEDURES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_CREATE_TRIGGERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_CREATE_USER_DEFINED_FUNCTIONS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_DELETE_CONFLICTS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_DELETE_ITEMS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_DELETE_STORED_PROCEDURES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_DELETE_TRIGGERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_DELETE_USER_DEFINED_FUNCTIONS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_EXECUTE_QUERIES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_EXECUTE_STORED_PROCEDURES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_CONFLICTS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_FEEDS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_STORED_PROCEDURES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_TRIGGERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_READ_USER_DEFINED_FUNCTIONS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_REPLACE_ITEMS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_REPLACE_STORED_PROCEDURES_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_REPLACE_TRIGGERS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_REPLACE_USER_DEFINED_FUNCTIONS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_UPSERT_ITEMS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_CONTAINER_WRITE_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ITEM_DELETE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ITEM_READ_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ITEM_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ITEM_REPLACE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ITEM_UPSERT_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_ITEM_WRITE_ALL_ACCESS_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_STORED_PROCEDURE_DELETE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_STORED_PROCEDURE_EXECUTE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_STORED_PROCEDURE_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_STORED_PROCEDURE_REPLACE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_TRIGGER_DELETE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_TRIGGER_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_TRIGGER_REPLACE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_USER_DEFINED_FUNCTION_DELETE_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_USER_DEFINED_FUNCTION_READ_VALUE;
import static com.azure.cosmos.implementation.sastokens.PermissionScopeValues.SCOPE_USER_DEFINED_FUNCTION_REPLACE_VALUE;

/**
 * Represents permission scope settings applicable to data plane related operations.
 */
public enum DataPlanePermissionScope {
    // REQUIRED: This enum must be kept in sync with the DataPlanePermissionScope enum in backend services.

    /**
     * Cosmos Container read scope.
     */
    SCOPE_CONTAINER_EXECUTE_QUERIES("ContainerExecuteQueriesFeeds", SCOPE_CONTAINER_EXECUTE_QUERIES_VALUE),
    SCOPE_CONTAINER_READ_FEEDS("ContainerReadFeeds", SCOPE_CONTAINER_READ_FEEDS_VALUE),
    SCOPE_CONTAINER_READ_STORED_PROCEDURES("ContainerReadStoredProcedures", SCOPE_CONTAINER_READ_STORED_PROCEDURES_VALUE),
    SCOPE_CONTAINER_READ_USER_DEFINED_FUNCTIONS("ContainerUserDefinedFunctions", SCOPE_CONTAINER_READ_USER_DEFINED_FUNCTIONS_VALUE),
    SCOPE_CONTAINER_READ_TRIGGERS("ContainerReadTriggers", SCOPE_CONTAINER_READ_TRIGGERS_VALUE),
    SCOPE_CONTAINER_READ_CONFLICTS("ContainerReadConflicts", SCOPE_CONTAINER_READ_CONFLICTS_VALUE),
    SCOPE_ITEM_READ("ItemRead", SCOPE_ITEM_READ_VALUE),
    SCOPE_STORED_PROCEDURE_READ("StoreProcedureRead", SCOPE_STORED_PROCEDURE_READ_VALUE),
    SCOPE_USER_DEFINED_FUNCTION_READ("UserDefinedFunctionRead", SCOPE_USER_DEFINED_FUNCTION_READ_VALUE),
    SCOPE_TRIGGER_READ("TriggerRead", SCOPE_TRIGGER_READ_VALUE),

    /**
     * Cosmos Container read scope.
     */
    SCOPE_CONTAINER_CREATE_ITEMS("ContainerCreateItems", SCOPE_CONTAINER_CREATE_ITEMS_VALUE),
    SCOPE_CONTAINER_REPLACE_ITEMS("ContainerReplaceItems", SCOPE_CONTAINER_REPLACE_ITEMS_VALUE),
    SCOPE_CONTAINER_UPSERT_ITEMS("ContainerUpsertItems", SCOPE_CONTAINER_UPSERT_ITEMS_VALUE),
    SCOPE_CONTAINER_DELETE_ITEMS("ContainerDeleteItems", SCOPE_CONTAINER_DELETE_ITEMS_VALUE),
    SCOPE_CONTAINER_CREATE_STORED_PROCEDURES("ContainerCreateStoredProcedures", SCOPE_CONTAINER_CREATE_STORED_PROCEDURES_VALUE),
    SCOPE_CONTAINER_REPLACE_STORED_PROCEDURES("ContainerReplaceStoredProcedures", SCOPE_CONTAINER_REPLACE_STORED_PROCEDURES_VALUE),
    SCOPE_CONTAINER_DELETE_STORED_PROCEDURES("ContainerDeleteStoredProcedures", SCOPE_CONTAINER_DELETE_STORED_PROCEDURES_VALUE),
    SCOPE_CONTAINER_EXECUTE_STORED_PROCEDURES("ContainerDeleteStoredProcedures", SCOPE_CONTAINER_EXECUTE_STORED_PROCEDURES_VALUE),
    SCOPE_CONTAINER_CREATE_TRIGGERS("ContainerCreateTriggers", SCOPE_CONTAINER_CREATE_TRIGGERS_VALUE),
    SCOPE_CONTAINER_REPLACE_TRIGGERS("ContainerReplaceTriggers", SCOPE_CONTAINER_REPLACE_TRIGGERS_VALUE),
    SCOPE_CONTAINER_DELETE_TRIGGERS("ContainerDeleteTriggers", SCOPE_CONTAINER_DELETE_TRIGGERS_VALUE),
    SCOPE_CONTAINER_CREATE_USER_DEFINED_FUNCTIONS("ContainerCreateUserDefinedFunctions", SCOPE_CONTAINER_CREATE_USER_DEFINED_FUNCTIONS_VALUE),
    SCOPE_CONTAINER_REPLACE_USER_DEFINED_FUNCTIONS("ContainerReplaceUserDefinedFunctions", SCOPE_CONTAINER_REPLACE_USER_DEFINED_FUNCTIONS_VALUE),
    SCOPE_CONTAINER_DELETE_USER_DEFINED_FUNCTIONS("ContainerCreateUserDefinedFunctions", SCOPE_CONTAINER_DELETE_USER_DEFINED_FUNCTIONS_VALUE),
    SCOPE_CONTAINER_DELETE_CONFLICTS("ContainerDeleteConflics", SCOPE_CONTAINER_DELETE_CONFLICTS_VALUE),
    SCOPE_ITEM_REPLACE("ItemReplace", SCOPE_ITEM_REPLACE_VALUE),
    SCOPE_ITEM_UPSERT("ItemUpsert", SCOPE_ITEM_UPSERT_VALUE),
    SCOPE_ITEM_DELETE("ItemDelete", SCOPE_ITEM_DELETE_VALUE),
    SCOPE_STORED_PROCEDURE_REPLACE("StoredProcedureReplace", SCOPE_STORED_PROCEDURE_REPLACE_VALUE),
    SCOPE_STORED_PROCEDURE_DELETE("StoredProcedureReplace", SCOPE_STORED_PROCEDURE_DELETE_VALUE),
    SCOPE_STORED_PROCEDURE_EXECUTE("StoredProcedureReplace", SCOPE_STORED_PROCEDURE_EXECUTE_VALUE),
    SCOPE_USER_DEFINED_FUNCTION_REPLACE("UserDefinedFunctionReplace", SCOPE_USER_DEFINED_FUNCTION_REPLACE_VALUE),
    SCOPE_USER_DEFINED_FUNCTION_DELETE("UserDefinedFunctionReplace", SCOPE_USER_DEFINED_FUNCTION_DELETE_VALUE),
    SCOPE_TRIGGER_REPLACE("TriggerReplace", SCOPE_TRIGGER_REPLACE_VALUE),
    SCOPE_TRIGGER_DELETE("TriggerDelete", SCOPE_TRIGGER_DELETE_VALUE),

    /**
     * Composite read scope.
     */
    SCOPE_CONTAINER_READ_ALL_ACCESS("ContainerReadAllAccess", SCOPE_CONTAINER_READ_ALL_ACCESS_VALUE),
    SCOPE_ITEM_READ_ALL_ACCESS("ItemReadAllAccess", SCOPE_ITEM_READ_ALL_ACCESS_VALUE),

    /**
     * Composite write scope.
     */
    SCOPE_CONTAINER_WRITE_ALL_ACCESS("ContainerWriteAllAccess", SCOPE_CONTAINER_WRITE_ALL_ACCESS_VALUE),
    SCOPE_ITEM_WRITE_ALL_ACCESS("ItemWriteAllAccess", SCOPE_ITEM_WRITE_ALL_ACCESS_VALUE),

    NONE("None", 0x0);


    private final int value;
    private final String stringValue;
    private final String toLowerStringValue;

    DataPlanePermissionScope(String stringValue, int scopeBitMask) {
        this.stringValue = stringValue;
        this.toLowerStringValue = stringValue.toLowerCase(Locale.ROOT);
        this.value = scopeBitMask;
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

    public String toLowerCase() {
        return this.toLowerStringValue;
    }

    public int value() {
        return this.value;
    }
}
