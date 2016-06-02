/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL database item.
 */
public class USqlDatabaseInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    @JsonProperty(value = "databaseName")
    private String name;

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
     * @return the USqlDatabaseInner object itself.
     */
    public USqlDatabaseInner withName(String name) {
        this.name = name;
        return this;
    }

}
