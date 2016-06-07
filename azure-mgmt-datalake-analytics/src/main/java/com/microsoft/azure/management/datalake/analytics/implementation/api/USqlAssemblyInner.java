/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL Assembly.
 */
public class USqlAssemblyInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the assembly.
     */
    @JsonProperty(value = "assemblyName")
    private String name;

    /**
     * Gets or sets the name of the CLR.
     */
    private String clrName;

    /**
     * Gets or sets the switch indicating if this assembly is visible or not.
     */
    private Boolean isVisible;

    /**
     * Gets or sets the switch indicating if this assembly is user defined or
     * not.
     */
    private Boolean isUserDefined;

    /**
     * Gets or sets the list of files associated with the assembly.
     */
    private List<USqlAssemblyFileInfo> files;

    /**
     * Gets or sets the list of dependencies associated with the assembly.
     */
    private List<USqlAssemblyDependencyInfo> dependencies;

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
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withDatabaseName(String databaseName) {
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
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withName(String name) {
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
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withClrName(String clrName) {
        this.clrName = clrName;
        return this;
    }

    /**
     * Get the isVisible value.
     *
     * @return the isVisible value
     */
    public Boolean isVisible() {
        return this.isVisible;
    }

    /**
     * Set the isVisible value.
     *
     * @param isVisible the isVisible value to set
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
        return this;
    }

    /**
     * Get the isUserDefined value.
     *
     * @return the isUserDefined value
     */
    public Boolean isUserDefined() {
        return this.isUserDefined;
    }

    /**
     * Set the isUserDefined value.
     *
     * @param isUserDefined the isUserDefined value to set
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withIsUserDefined(Boolean isUserDefined) {
        this.isUserDefined = isUserDefined;
        return this;
    }

    /**
     * Get the files value.
     *
     * @return the files value
     */
    public List<USqlAssemblyFileInfo> files() {
        return this.files;
    }

    /**
     * Set the files value.
     *
     * @param files the files value to set
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withFiles(List<USqlAssemblyFileInfo> files) {
        this.files = files;
        return this;
    }

    /**
     * Get the dependencies value.
     *
     * @return the dependencies value
     */
    public List<USqlAssemblyDependencyInfo> dependencies() {
        return this.dependencies;
    }

    /**
     * Set the dependencies value.
     *
     * @param dependencies the dependencies value to set
     * @return the USqlAssemblyInner object itself.
     */
    public USqlAssemblyInner withDependencies(List<USqlAssemblyDependencyInfo> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

}
