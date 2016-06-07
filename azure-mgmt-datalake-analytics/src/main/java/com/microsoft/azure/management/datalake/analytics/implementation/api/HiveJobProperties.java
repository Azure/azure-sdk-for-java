/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The HiveJobProperties model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("Hive")
public class HiveJobProperties extends JobProperties {
    /**
     * Gets or sets the statement information for each statement in the script.
     */
    private List<HiveJobStatementInfo> statementInfo;

    /**
     * Gets or sets the Hive logs location.
     */
    private String logsLocation;

    /**
     * Gets or sets the location of the Hive warehouse.
     */
    private String warehouseLocation;

    /**
     * Gets or sets the number of statements that will be run based on the
     * script.
     */
    private Integer statementCount;

    /**
     * Gets or sets the number of statements that have been run based on the
     * script.
     */
    private Integer executedStatementCount;

    /**
     * Get the statementInfo value.
     *
     * @return the statementInfo value
     */
    public List<HiveJobStatementInfo> statementInfo() {
        return this.statementInfo;
    }

    /**
     * Set the statementInfo value.
     *
     * @param statementInfo the statementInfo value to set
     * @return the HiveJobProperties object itself.
     */
    public HiveJobProperties withStatementInfo(List<HiveJobStatementInfo> statementInfo) {
        this.statementInfo = statementInfo;
        return this;
    }

    /**
     * Get the logsLocation value.
     *
     * @return the logsLocation value
     */
    public String logsLocation() {
        return this.logsLocation;
    }

    /**
     * Set the logsLocation value.
     *
     * @param logsLocation the logsLocation value to set
     * @return the HiveJobProperties object itself.
     */
    public HiveJobProperties withLogsLocation(String logsLocation) {
        this.logsLocation = logsLocation;
        return this;
    }

    /**
     * Get the warehouseLocation value.
     *
     * @return the warehouseLocation value
     */
    public String warehouseLocation() {
        return this.warehouseLocation;
    }

    /**
     * Set the warehouseLocation value.
     *
     * @param warehouseLocation the warehouseLocation value to set
     * @return the HiveJobProperties object itself.
     */
    public HiveJobProperties withWarehouseLocation(String warehouseLocation) {
        this.warehouseLocation = warehouseLocation;
        return this;
    }

    /**
     * Get the statementCount value.
     *
     * @return the statementCount value
     */
    public Integer statementCount() {
        return this.statementCount;
    }

    /**
     * Set the statementCount value.
     *
     * @param statementCount the statementCount value to set
     * @return the HiveJobProperties object itself.
     */
    public HiveJobProperties withStatementCount(Integer statementCount) {
        this.statementCount = statementCount;
        return this;
    }

    /**
     * Get the executedStatementCount value.
     *
     * @return the executedStatementCount value
     */
    public Integer executedStatementCount() {
        return this.executedStatementCount;
    }

    /**
     * Set the executedStatementCount value.
     *
     * @param executedStatementCount the executedStatementCount value to set
     * @return the HiveJobProperties object itself.
     */
    public HiveJobProperties withExecutedStatementCount(Integer executedStatementCount) {
        this.executedStatementCount = executedStatementCount;
        return this;
    }

}
