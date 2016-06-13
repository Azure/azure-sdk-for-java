/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL procedure item.
 */
public class USqlProcedure extends CatalogItem {
    /**
     * the name of the database.
     */
    private String databaseName;

    /**
     * the name of the schema associated with this procedure and database.
     */
    private String schemaName;

    /**
     * the name of the procedure.
     */
    @JsonProperty(value = "procName")
    private String name;

    /**
     * the defined query of the procedure.
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
     * @return the USqlProcedure object itself.
     */
    public USqlProcedure withDatabaseName(String databaseName) {
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
     * @return the USqlProcedure object itself.
     */
    public USqlProcedure withSchemaName(String schemaName) {
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
     * @return the USqlProcedure object itself.
     */
    public USqlProcedure withName(String name) {
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
     * @return the USqlProcedure object itself.
     */
    public USqlProcedure withDefinition(String definition) {
        this.definition = definition;
        return this;
    }

}
