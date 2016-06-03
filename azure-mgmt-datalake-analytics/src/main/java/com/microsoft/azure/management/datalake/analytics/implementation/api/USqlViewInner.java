/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL view item.
 */
public class USqlViewInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the schema associated with this view and
     * database.
     */
    private String schemaName;

    /**
     * Gets or sets the name of the view.
     */
    @JsonProperty(value = "viewName")
    private String name;

    /**
     * Gets or sets the defined query of the view.
     */
    private String definition;

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
     * @return the USqlViewInner object itself.
     */
    public USqlViewInner withDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    /**
     * Get the schemaName value.
     *
     * @return the schemaName value
     */
    public String schemaName() {
        return this.schemaName;
    }

    /**
     * Set the schemaName value.
     *
     * @param schemaName the schemaName value to set
     * @return the USqlViewInner object itself.
     */
    public USqlViewInner withSchemaName(String schemaName) {
        this.schemaName = schemaName;
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
     * @return the USqlViewInner object itself.
     */
    public USqlViewInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the definition value.
     *
     * @return the definition value
     */
    public String definition() {
        return this.definition;
    }

    /**
     * Set the definition value.
     *
     * @param definition the definition value to set
     * @return the USqlViewInner object itself.
     */
    public USqlViewInner withDefinition(String definition) {
        this.definition = definition;
        return this;
    }

}
