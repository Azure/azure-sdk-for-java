// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The rollup settings for the data feed.
 */
@Fluent
public final class DataFeedRollupSettings {
    private DataFeedRollupType dataFeedRollupType;
    private List<String> autoRollupGroupByColumnNames;
    private String rollupIdentificationValue;
    private DataFeedAutoRollUpMethod dataFeedAutoRollUpMethod;

    /**
     * Get the rollup type that should be used for the data feed.
     *
     * @return the dataFeedRollupType value.
     */
    public DataFeedRollupType getRollupType() {
        return this.dataFeedRollupType;
    }

    /**
     * Get the column names for which the auto rollup setting will group by.
     *
     * @return the autoRollupGroupByColumnNames value.
     */
    public List<String> getAutoRollupGroupByColumnNames() {
        return this.autoRollupGroupByColumnNames;
    }

    /**
     * Get the identification value when using auto rollup.
     *
     * @return the alreadyRollupIdentificationValue value.
     */
    public String getRollupIdentificationValue() {
        return this.rollupIdentificationValue;
    }

    /**
     * Get the rollup method that should be used for the data feed.
     *
     * @return the dataFeedAutoRollUpMethod value.
     */
    public DataFeedAutoRollUpMethod getDataFeedAutoRollUpMethod() {
        return this.dataFeedAutoRollUpMethod;
    }

    /**
     * Set the rollup type settings that should be used for the data feed.
     *
     * @param dataFeedRollupType the rollup type settings value to set.
     *
     * @return the DataFeedRollupSettings object itself.
     */
    public DataFeedRollupSettings setRollupType(DataFeedRollupType dataFeedRollupType) {
        this.dataFeedRollupType = dataFeedRollupType;
        return this;
    }

    /**
     * Set up rollup settings to be used for the data feed aggregation.
     *
     * @param rollUpMethod the rollup method value to set.
     * @param groupByColumnNames the column names for the group by.
     *
     * @return the DataFeedRollupSettings object itself.
     */
    public DataFeedRollupSettings setAutoRollup(DataFeedAutoRollUpMethod rollUpMethod,
        List<String> groupByColumnNames) {
        this.autoRollupGroupByColumnNames = groupByColumnNames;
        this.dataFeedAutoRollUpMethod = rollUpMethod;
        this.dataFeedRollupType = DataFeedRollupType.AUTO_ROLLUP;
        return this;
    }

    /**
     * Set up rollup settings to be used for the data feed aggregation.
     *
     * @param rollUpMethod the rollup method value to set.
     * @param groupByColumnNames the column names for the group by.
     * @param rollupIdentificationValue the raw group column value.
     *
     * @return the DataFeedRollupSettings object itself.
     */
    public DataFeedRollupSettings setAutoRollup(DataFeedAutoRollUpMethod rollUpMethod,
        List<String> groupByColumnNames,
        String rollupIdentificationValue) {
        this.autoRollupGroupByColumnNames = groupByColumnNames;
        this.rollupIdentificationValue = rollupIdentificationValue;
        this.dataFeedRollupType = DataFeedRollupType.AUTO_ROLLUP;
        return this;
    }

    /**
     * Set the identification value when rollup settings set to Auto or AlreadyRollUp.
     *
     * @param rollupIdentificationValue the identification value to set.
     *
     * @return the DataFeedRollupSettings object itself.
     */
    public DataFeedRollupSettings setAlreadyRollup(String rollupIdentificationValue) {
        this.rollupIdentificationValue = rollupIdentificationValue;
        this.dataFeedRollupType = DataFeedRollupType.ALREADY_ROLLUP;
        return this;
    }
}
