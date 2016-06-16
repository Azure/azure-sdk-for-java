/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for DatabaseServerType.
 */
public enum DatabaseServerType {
    /** Enum value MySql. */
    MY_SQL("MySql"),

    /** Enum value SQLServer. */
    SQLSERVER("SQLServer"),

    /** Enum value SQLAzure. */
    SQLAZURE("SQLAzure"),

    /** Enum value Custom. */
    CUSTOM("Custom");

    /** The actual serialized value for a DatabaseServerType instance. */
    private String value;

    DatabaseServerType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a DatabaseServerType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed DatabaseServerType object, or null if unable to parse.
     */
    @JsonCreator
    public static DatabaseServerType fromString(String value) {
        DatabaseServerType[] items = DatabaseServerType.values();
        for (DatabaseServerType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
