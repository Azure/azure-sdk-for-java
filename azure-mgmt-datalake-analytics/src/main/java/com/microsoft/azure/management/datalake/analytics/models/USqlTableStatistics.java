/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import org.joda.time.DateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL table statistics item.
 */
public class USqlTableStatistics extends CatalogItem {
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
    private String tableName;

    /**
     * Gets or sets the name of the table statistics.
     */
    @JsonProperty(value = "statisticsName")
    private String name;

    /**
     * Gets or sets the name of the user statistics.
     */
    private String userStatName;

    /**
     * Gets or sets the path to the statistics data.
     */
    private String statDataPath;

    /**
     * Gets or sets the creation time of the statistics.
     */
    private DateTime createTime;

    /**
     * Gets or sets the last time the statistics were updated.
     */
    private DateTime updateTime;

    /**
     * Gets or sets the switch indicating if these statistics are user created.
     */
    private Boolean isUserCreated;

    /**
     * Gets or sets the switch indicating if these statistics are
     * automatically created.
     */
    private Boolean isAutoCreated;

    /**
     * Gets or sets the switch indicating if these statistics have a filter.
     */
    private Boolean hasFilter;

    /**
     * Gets or sets the filter definition for the statistics.
     */
    private String filterDefinition;

    /**
     * Gets or sets the list of column names associated with these statistics.
     */
    private List<String> colNames;

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
     * Get the tableName value.
     *
     * @return the tableName value
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Set the tableName value.
     *
     * @param tableName the tableName value to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
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
     * Get the userStatName value.
     *
     * @return the userStatName value
     */
    public String getUserStatName() {
        return this.userStatName;
    }

    /**
     * Set the userStatName value.
     *
     * @param userStatName the userStatName value to set
     */
    public void setUserStatName(String userStatName) {
        this.userStatName = userStatName;
    }

    /**
     * Get the statDataPath value.
     *
     * @return the statDataPath value
     */
    public String getStatDataPath() {
        return this.statDataPath;
    }

    /**
     * Set the statDataPath value.
     *
     * @param statDataPath the statDataPath value to set
     */
    public void setStatDataPath(String statDataPath) {
        this.statDataPath = statDataPath;
    }

    /**
     * Get the createTime value.
     *
     * @return the createTime value
     */
    public DateTime getCreateTime() {
        return this.createTime;
    }

    /**
     * Set the createTime value.
     *
     * @param createTime the createTime value to set
     */
    public void setCreateTime(DateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * Get the updateTime value.
     *
     * @return the updateTime value
     */
    public DateTime getUpdateTime() {
        return this.updateTime;
    }

    /**
     * Set the updateTime value.
     *
     * @param updateTime the updateTime value to set
     */
    public void setUpdateTime(DateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * Get the isUserCreated value.
     *
     * @return the isUserCreated value
     */
    public Boolean getIsUserCreated() {
        return this.isUserCreated;
    }

    /**
     * Set the isUserCreated value.
     *
     * @param isUserCreated the isUserCreated value to set
     */
    public void setIsUserCreated(Boolean isUserCreated) {
        this.isUserCreated = isUserCreated;
    }

    /**
     * Get the isAutoCreated value.
     *
     * @return the isAutoCreated value
     */
    public Boolean getIsAutoCreated() {
        return this.isAutoCreated;
    }

    /**
     * Set the isAutoCreated value.
     *
     * @param isAutoCreated the isAutoCreated value to set
     */
    public void setIsAutoCreated(Boolean isAutoCreated) {
        this.isAutoCreated = isAutoCreated;
    }

    /**
     * Get the hasFilter value.
     *
     * @return the hasFilter value
     */
    public Boolean getHasFilter() {
        return this.hasFilter;
    }

    /**
     * Set the hasFilter value.
     *
     * @param hasFilter the hasFilter value to set
     */
    public void setHasFilter(Boolean hasFilter) {
        this.hasFilter = hasFilter;
    }

    /**
     * Get the filterDefinition value.
     *
     * @return the filterDefinition value
     */
    public String getFilterDefinition() {
        return this.filterDefinition;
    }

    /**
     * Set the filterDefinition value.
     *
     * @param filterDefinition the filterDefinition value to set
     */
    public void setFilterDefinition(String filterDefinition) {
        this.filterDefinition = filterDefinition;
    }

    /**
     * Get the colNames value.
     *
     * @return the colNames value
     */
    public List<String> getColNames() {
        return this.colNames;
    }

    /**
     * Set the colNames value.
     *
     * @param colNames the colNames value to set
     */
    public void setColNames(List<String> colNames) {
        this.colNames = colNames;
    }

}
