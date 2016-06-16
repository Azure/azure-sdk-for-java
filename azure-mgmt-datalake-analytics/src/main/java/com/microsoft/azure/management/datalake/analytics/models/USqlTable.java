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
     * the name of the database.
     */
    private String databaseName;

    /**
     * the name of the schema associated with this table and database.
     */
    private String schemaName;

    /**
     * the name of the table.
     */
    @JsonProperty(value = "tableName")
    private String name;

    /**
     * the list of columns in this table.
     */
    private List<USqlTableColumn> columnList;

    /**
     * the list of indices in this table.
     */
    private List<USqlIndex> indexList;

    /**
     * the list of partition keys in the table.
     */
    private List<String> partitionKeyList;

    /**
     * the external table associated with the table.
     */
    private ExternalTable externalTable;

    /**
     * the distributions info of the table.
     */
    private USqlDistributionInfo distributionInfo;

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
     * @return the USqlTable object itself.
     */
    public USqlTable withDatabaseName(String databaseName) {
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
     * @return the USqlTable object itself.
     */
    public USqlTable withSchemaName(String schemaName) {
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
     * @return the USqlTable object itself.
     */
    public USqlTable withName(String name) {
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
     * @return the USqlTable object itself.
     */
    public USqlTable withColumnList(List<USqlTableColumn> columnList) {
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
     * @return the USqlTable object itself.
     */
    public USqlTable withIndexList(List<USqlIndex> indexList) {
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
     * @return the USqlTable object itself.
     */
    public USqlTable withPartitionKeyList(List<String> partitionKeyList) {
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
     * @return the USqlTable object itself.
     */
    public USqlTable withExternalTable(ExternalTable externalTable) {
        this.externalTable = externalTable;
        return this;
    }

    /**
     * Get the distributionInfo value.
     *
     * @return the distributionInfo value
     */
    public USqlDistributionInfo distributionInfo() {
        return this.distributionInfo;
    }

    /**
     * Set the distributionInfo value.
     *
     * @param distributionInfo the distributionInfo value to set
     * @return the USqlTable object itself.
     */
    public USqlTable withDistributionInfo(USqlDistributionInfo distributionInfo) {
        this.distributionInfo = distributionInfo;
        return this;
    }

}
