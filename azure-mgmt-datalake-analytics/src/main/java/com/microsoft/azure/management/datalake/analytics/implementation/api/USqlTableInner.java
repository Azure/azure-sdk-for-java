/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL table item.
 */
public class USqlTableInner extends CatalogItem {
    /**
     * Gets or sets the name of the database.
     */
    private String databaseName;

    /**
     * Gets or sets the name of the schema associated with this table and
     * database.
     */
    private String schemaName;

    /**
     * Gets or sets the name of the table.
     */
    @JsonProperty(value = "tableName")
    private String name;

    /**
     * Gets or sets the list of columns in this table.
     */
    private List<USqlTableColumn> columnList;

    /**
     * Gets or sets the list of indices in this table.
     */
    private List<USqlIndex> indexList;

    /**
     * Gets or sets the list of partition keys in the table.
     */
    private List<String> partitionKeyList;

    /**
     * Gets or sets the external table associated with the table.
     */
    private ExternalTable externalTable;

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
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withDatabaseName(String databaseName) {
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
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withSchemaName(String schemaName) {
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
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the columnList value.
     *
     * @return the columnList value
     */
    public List<USqlTableColumn> columnList() {
        return this.columnList;
    }

    /**
     * Set the columnList value.
     *
     * @param columnList the columnList value to set
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withColumnList(List<USqlTableColumn> columnList) {
        this.columnList = columnList;
        return this;
    }

    /**
     * Get the indexList value.
     *
     * @return the indexList value
     */
    public List<USqlIndex> indexList() {
        return this.indexList;
    }

    /**
     * Set the indexList value.
     *
     * @param indexList the indexList value to set
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withIndexList(List<USqlIndex> indexList) {
        this.indexList = indexList;
        return this;
    }

    /**
     * Get the partitionKeyList value.
     *
     * @return the partitionKeyList value
     */
    public List<String> partitionKeyList() {
        return this.partitionKeyList;
    }

    /**
     * Set the partitionKeyList value.
     *
     * @param partitionKeyList the partitionKeyList value to set
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withPartitionKeyList(List<String> partitionKeyList) {
        this.partitionKeyList = partitionKeyList;
        return this;
    }

    /**
     * Get the externalTable value.
     *
     * @return the externalTable value
     */
    public ExternalTable externalTable() {
        return this.externalTable;
    }

    /**
     * Set the externalTable value.
     *
     * @param externalTable the externalTable value to set
     * @return the USqlTableInner object itself.
     */
    public USqlTableInner withExternalTable(ExternalTable externalTable) {
        this.externalTable = externalTable;
        return this;
    }

}
