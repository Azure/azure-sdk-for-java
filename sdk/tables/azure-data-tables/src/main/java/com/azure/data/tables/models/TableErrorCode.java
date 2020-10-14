// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for error codes returned from the Tables service.
 */
public class TableErrorCode extends ExpandableStringEnum<TableErrorCode> {

    /**
     * Static value {@code TableAlreadyExists}.
     */
    public static final TableErrorCode TABLE_ALREADY_EXISTS = fromString("TableAlreadyExists");

    /**
     * Static value {@code TableNotFound}.
     */
    public static final TableErrorCode TABLE_NOT_FOUND = fromString("TableNotFound");

    /**
     * Static value {@code InvalidTableName}.
     */
    public static final TableErrorCode INVALID_TABLE_NAME = fromString("InvalidTableName");

    /**
     * Static value {@code EntityAlreadyExists}.
     */
    public static final TableErrorCode ENTITY_ALREADY_EXISTS = fromString("EntityAlreadyExists");

    /**
     * Static value {@code EntityNotFound}.
     */
    public static final TableErrorCode ENTITY_NOT_FOUND = fromString("EntityNotFound");

    /**
     * Static value {@code InvalidPkOrRkName}.
     */
    public static final TableErrorCode INVALID_PK_OR_RK_NAME = fromString("InvalidPkOrRkName");

    /**
     * Returns the {@code TableErrorCode} constant with the provided name, or {@code null} if no {@code TableErrorCode}
     * has the provided name.
     *
     * @param name The name of the error.
     * @return The {@code TableErrorCode} value having the provided name.
     */
    public static TableErrorCode fromString(String name) {
        return fromString(name, TableErrorCode.class);
    }
}
