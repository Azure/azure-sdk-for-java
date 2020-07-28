// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.data.tables.TableClient;

/**
 * class for a table object
 */
public class Table {
    private final String name;
    private TableClient tableClient;

    /**
     * crete a table
     * @param name the name of the table
     */
    public Table(String name) {
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
