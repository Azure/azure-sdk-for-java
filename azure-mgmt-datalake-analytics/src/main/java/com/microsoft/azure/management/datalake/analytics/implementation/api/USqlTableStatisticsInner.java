/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import org.joda.time.DateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Data Lake Analytics catalog U-SQL table statistics item.
 */
public class USqlTableStatisticsInner extends CatalogItem {
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
    public String databaseName() {
        return this.databaseName;
    }

    /**
     * Set the databaseName value.
     *
     * @param databaseName the databaseName value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withDatabaseName(String databaseName) {
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
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withSchemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    /**
     * Get the tableName value.
     *
     * @return the tableName value
     */
    public String tableName() {
        return this.tableName;
    }

    /**
     * Set the tableName value.
     *
     * @param tableName the tableName value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withTableName(String tableName) {
        this.tableName = tableName;
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
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the userStatName value.
     *
     * @return the userStatName value
     */
    public String userStatName() {
        return this.userStatName;
    }

    /**
     * Set the userStatName value.
     *
     * @param userStatName the userStatName value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withUserStatName(String userStatName) {
        this.userStatName = userStatName;
        return this;
    }

    /**
     * Get the statDataPath value.
     *
     * @return the statDataPath value
     */
    public String statDataPath() {
        return this.statDataPath;
    }

    /**
     * Set the statDataPath value.
     *
     * @param statDataPath the statDataPath value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withStatDataPath(String statDataPath) {
        this.statDataPath = statDataPath;
        return this;
    }

    /**
     * Get the createTime value.
     *
     * @return the createTime value
     */
    public DateTime createTime() {
        return this.createTime;
    }

    /**
     * Set the createTime value.
     *
     * @param createTime the createTime value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withCreateTime(DateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    /**
     * Get the updateTime value.
     *
     * @return the updateTime value
     */
    public DateTime updateTime() {
        return this.updateTime;
    }

    /**
     * Set the updateTime value.
     *
     * @param updateTime the updateTime value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withUpdateTime(DateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    /**
     * Get the isUserCreated value.
     *
     * @return the isUserCreated value
     */
    public Boolean isUserCreated() {
        return this.isUserCreated;
    }

    /**
     * Set the isUserCreated value.
     *
     * @param isUserCreated the isUserCreated value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withIsUserCreated(Boolean isUserCreated) {
        this.isUserCreated = isUserCreated;
        return this;
    }

    /**
     * Get the isAutoCreated value.
     *
     * @return the isAutoCreated value
     */
    public Boolean isAutoCreated() {
        return this.isAutoCreated;
    }

    /**
     * Set the isAutoCreated value.
     *
     * @param isAutoCreated the isAutoCreated value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withIsAutoCreated(Boolean isAutoCreated) {
        this.isAutoCreated = isAutoCreated;
        return this;
    }

    /**
     * Get the hasFilter value.
     *
     * @return the hasFilter value
     */
    public Boolean hasFilter() {
        return this.hasFilter;
    }

    /**
     * Set the hasFilter value.
     *
     * @param hasFilter the hasFilter value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withHasFilter(Boolean hasFilter) {
        this.hasFilter = hasFilter;
        return this;
    }

    /**
     * Get the filterDefinition value.
     *
     * @return the filterDefinition value
     */
    public String filterDefinition() {
        return this.filterDefinition;
    }

    /**
     * Set the filterDefinition value.
     *
     * @param filterDefinition the filterDefinition value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withFilterDefinition(String filterDefinition) {
        this.filterDefinition = filterDefinition;
        return this;
    }

    /**
     * Get the colNames value.
     *
     * @return the colNames value
     */
    public List<String> colNames() {
        return this.colNames;
    }

    /**
     * Set the colNames value.
     *
     * @param colNames the colNames value to set
     * @return the USqlTableStatisticsInner object itself.
     */
    public USqlTableStatisticsInner withColNames(List<String> colNames) {
        this.colNames = colNames;
        return this;
    }

}
