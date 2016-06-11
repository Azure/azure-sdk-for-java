/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL database item.
 */
public class USqlDatabase extends CatalogItem {
    /**
     * the name of the database.
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
     * @return the USqlDatabase object itself.
     */
    public USqlDatabase withName(String name) {
        this.name = name;
        return this;
    }

}
