/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL table item.
 */
public class USqlTable extends CatalogItem {
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
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Get the schemaName value.
     *
     * @return the schemaName value
     */
    public String getSchemaName() {
        return this.schemaName;
    }

    /**
     * Set the schemaName value.
     *
     * @param schemaName the schemaName value to set
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the columnList value.
     *
     * @return the columnList value
     */
    public List<USqlTableColumn> getColumnList() {
        return this.columnList;
    }

    /**
     * Set the columnList value.
     *
     * @param columnList the columnList value to set
     */
    public void setColumnList(List<USqlTableColumn> columnList) {
        this.columnList = columnList;
    }

    /**
     * Get the indexList value.
     *
     * @return the indexList value
     */
    public List<USqlIndex> getIndexList() {
        return this.indexList;
    }

    /**
     * Set the indexList value.
     *
     * @param indexList the indexList value to set
     */
    public void setIndexList(List<USqlIndex> indexList) {
        this.indexList = indexList;
    }

    /**
     * Get the partitionKeyList value.
     *
     * @return the partitionKeyList value
     */
    public List<String> getPartitionKeyList() {
        return this.partitionKeyList;
    }

    /**
     * Set the partitionKeyList value.
     *
     * @param partitionKeyList the partitionKeyList value to set
     */
    public void setPartitionKeyList(List<String> partitionKeyList) {
        this.partitionKeyList = partitionKeyList;
    }

    /**
     * Get the externalTable value.
     *
     * @return the externalTable value
     */
    public ExternalTable getExternalTable() {
        return this.externalTable;
    }

    /**
     * Set the externalTable value.
     *
     * @param externalTable the externalTable value to set
     */
    public void setExternalTable(ExternalTable externalTable) {
        this.externalTable = externalTable;
    }

}
