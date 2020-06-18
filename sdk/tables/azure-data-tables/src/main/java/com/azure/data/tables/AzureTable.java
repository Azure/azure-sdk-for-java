// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

public class AzureTable {
    private final String name;

    AzureTable(String name) {
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
