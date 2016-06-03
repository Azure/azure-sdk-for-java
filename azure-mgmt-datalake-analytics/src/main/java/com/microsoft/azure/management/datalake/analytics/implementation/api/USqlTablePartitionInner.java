/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL table partition item.
 */
public class USqlTablePartitionInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the schema associated with this table
     * partition and database.
     */
    private String schemaName;

    /**
     * Gets or sets the name of the table partition.
     */
    @JsonProperty(value = "partitionName")
    private String name;

    /**
     * Gets or sets the index ID for this partition.
     */
    private Integer indexId;

    /**
     * Gets or sets the list of labels associated with this partition.
     */
    private List<String> label;

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
     * @return the USqlTablePartitionInner object itself.
     */
    public USqlTablePartitionInner withDatabaseName(String databaseName) {
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
     * @return the USqlTablePartitionInner object itself.
     */
    public USqlTablePartitionInner withSchemaName(String schemaName) {
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
     * @return the USqlTablePartitionInner object itself.
     */
    public USqlTablePartitionInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the indexId value.
     *
     * @return the indexId value
     */
    public Integer indexId() {
        return this.indexId;
    }

    /**
     * Set the indexId value.
     *
     * @param indexId the indexId value to set
     * @return the USqlTablePartitionInner object itself.
     */
    public USqlTablePartitionInner withIndexId(Integer indexId) {
        this.indexId = indexId;
        return this;
    }

    /**
     * Get the label value.
     *
     * @return the label value
     */
    public List<String> label() {
        return this.label;
    }

    /**
     * Set the label value.
     *
     * @param label the label value to set
     * @return the USqlTablePartitionInner object itself.
     */
    public USqlTablePartitionInner withLabel(List<String> label) {
        this.label = label;
        return this;
    }

}
