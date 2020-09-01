// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

/**
 * class for a table object
 */
public class TableItem {
    private final String name;

    /**
     * crete a table
     * @param name the name of the table
     */
    public TableItem(String name) {
        this.name = name;
    }

    /**
     * returns the name of this table
     *
     * @return table name
     */
    public String getName() {
        return name;
    }
}
