// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

public class AzureTable {
    private final String name;

    AzureTable(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this Table
     */
    public String getName() {
        return name;
    }
}
