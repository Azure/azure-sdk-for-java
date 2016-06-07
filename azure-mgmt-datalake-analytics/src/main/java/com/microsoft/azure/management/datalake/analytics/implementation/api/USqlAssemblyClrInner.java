/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL assembly CLR item.
 */
public class USqlAssemblyClrInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the assembly.
     */
    @JsonProperty(value = "assemblyClrName")
    private String name;

    /**
     * Gets or sets the name of the CLR.
     */
    private String clrName;

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
     * @return the USqlAssemblyClrInner object itself.
     */
    public USqlAssemblyClrInner withDatabaseName(String databaseName) {
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
     * @return the USqlAssemblyClrInner object itself.
     */
    public USqlAssemblyClrInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the clrName value.
     *
     * @return the clrName value
     */
    public String clrName() {
        return this.clrName;
    }

    /**
     * Set the clrName value.
     *
     * @param clrName the clrName value to set
     * @return the USqlAssemblyClrInner object itself.
     */
    public USqlAssemblyClrInner withClrName(String clrName) {
        this.clrName = clrName;
        return this;
    }

}
