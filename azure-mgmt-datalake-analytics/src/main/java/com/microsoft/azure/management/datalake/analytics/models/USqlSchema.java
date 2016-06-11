/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL schema item.
 */
public class USqlSchema extends CatalogItem {
    /**
     * the name of the database.
     */
    private String databaseName;

    /**
     * the name of the schema.
     */
    @JsonProperty(value = "schemaName")
    private String name;

    /**
     * Get the databaseName value.
     *
     * @return the databaseName value
     */
    public String databaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     * @return the USqlSchema object itself.
     */
    public USqlSchema withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the USqlSchema object itself.
     */
    public USqlSchema withName(String name) {
        this.name = name;
        return this;
    }

}
