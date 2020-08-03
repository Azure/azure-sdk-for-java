// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines values for TableErrorCode.
 */
public class TableErrorCode extends ExpandableStringEnum<TableErrorCode> {

    /**
     * Static value ResourceAlreadyExists for TableErrorCode.
     */
    public static final TableErrorCode TABLE_ALREADY_EXISTS = fromString("TableAlreadyExists");

    /**
     * Static value ResourceNotFound for TableErrorCode.
     */
    public static final TableErrorCode TABLE_NOT_FOUND = fromString("TableNotFound");

    /**
     * Static value InvalidResourceName for TableErrorCode.
     */
    public static final TableErrorCode INVALID_TABLE_NAME = fromString("InvalidTableName");

    /**
     * Static value ResourceAlreadyExists for TableErrorCode.
     */
    public static final TableErrorCode ENTITY_ALREADY_EXISTS = fromString("EntityAlreadyExists");

    /**
     * Static value ResourceNotFound for TableErrorCode.
     */
    public static final TableErrorCode ENTITY_NOT_FOUND = fromString("EntityNotFound");

    /**
     * Static value InvalidResourceName for TableErrorCode.
     */
    public static final TableErrorCode INVALID_PK_OR_RK_NAME = fromString("InvalidPkOrRkName");

    /**
     * returns an error code from a string
     * @param name the name of the error
     * @return the associated tableErrorCode
     */
    public static TableErrorCode fromString(String name) {
        return fromString(name, TableErrorCode.class);
    }

}
