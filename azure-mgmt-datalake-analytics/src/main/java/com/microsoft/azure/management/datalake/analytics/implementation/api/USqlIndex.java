/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import java.util.UUID;

/**
 * A Data Lake Analytics catalog U-SQL table index item.
 */
public class USqlIndex {
    /**
     * Gets or sets the name of the index in the table.
     */
    private String name;

    /**
     * Gets or sets the list of directed columns in the index.
     */
    private List<USqlDirectedColumn> indexKeys;

    /**
     * Gets or sets the list of columns in the index.
     */
    private List<String> columns;

    /**
     * Gets or sets the distributions info of the index.
     */
    private USqlDistributionInfo distributionInfo;

    /**
     * Gets or sets partition function ID for the index.
     */
    private UUID partitionFunction;

    /**
     * Gets or sets the list of partion keys in the index.
     */
    private List<String> partitionKeyList;

    /**
     * Gets or sets the list of full paths to the streams that contain this
     * index in the DataLake account.
     */
    private List<String> streamNames;

    /**
     * Gets or sets the switch indicating if this index is a columnstore index.
     */
    private Boolean isColumnstore;

    /**
     * Gets or sets the ID of this index within the table.
     */
    private Integer indexId;

    /**
     * Gets or sets the switch indicating if this index is a unique index.
     */
    private Boolean isUnique;

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
     * @return the USqlIndex object itself.
     */
    public USqlIndex withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the indexKeys value.
     *
     * @return the indexKeys value
     */
    public List<USqlDirectedColumn> indexKeys() {
        return this.indexKeys;
    }

    /**
     * Set the indexKeys value.
     *
     * @param indexKeys the indexKeys value to set
     * @return the USqlIndex object itself.
     */
    public USqlIndex withIndexKeys(List<USqlDirectedColumn> indexKeys) {
        this.indexKeys = indexKeys;
        return this;
    }

    /**
     * Get the columns value.
     *
     * @return the columns value
     */
    public List<String> columns() {
        return this.columns;
    }

    /**
     * Set the columns value.
     *
     * @param columns the columns value to set
     * @return the USqlIndex object itself.
     */
    public USqlIndex withColumns(List<String> columns) {
        this.columns = columns;
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
     * @return the USqlIndex object itself.
     */
    public USqlIndex withDistributionInfo(USqlDistributionInfo distributionInfo) {
        this.distributionInfo = distributionInfo;
        return this;
    }

    /**
     * Get the partitionFunction value.
     *
     * @return the partitionFunction value
     */
    public UUID partitionFunction() {
        return this.partitionFunction;
    }

    /**
     * Set the partitionFunction value.
     *
     * @param partitionFunction the partitionFunction value to set
     * @return the USqlIndex object itself.
     */
    public USqlIndex withPartitionFunction(UUID partitionFunction) {
        this.partitionFunction = partitionFunction;
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
     * @return the USqlIndex object itself.
     */
    public USqlIndex withPartitionKeyList(List<String> partitionKeyList) {
        this.partitionKeyList = partitionKeyList;
        return this;
    }

    /**
     * Get the streamNames value.
     *
     * @return the streamNames value
     */
    public List<String> streamNames() {
        return this.streamNames;
    }

    /**
     * Set the streamNames value.
     *
     * @param streamNames the streamNames value to set
     * @return the USqlIndex object itself.
     */
    public USqlIndex withStreamNames(List<String> streamNames) {
        this.streamNames = streamNames;
        return this;
    }

    /**
     * Get the isColumnstore value.
     *
     * @return the isColumnstore value
     */
    public Boolean isColumnstore() {
        return this.isColumnstore;
    }

    /**
     * Set the isColumnstore value.
     *
     * @param isColumnstore the isColumnstore value to set
     * @return the USqlIndex object itself.
     */
    public USqlIndex withIsColumnstore(Boolean isColumnstore) {
        this.isColumnstore = isColumnstore;
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
     * @return the USqlIndex object itself.
     */
    public USqlIndex withIndexId(Integer indexId) {
        this.indexId = indexId;
        return this;
    }

    /**
     * Get the isUnique value.
     *
     * @return the isUnique value
     */
    public Boolean isUnique() {
        return this.isUnique;
    }

    /**
     * Set the isUnique value.
     *
     * @param isUnique the isUnique value to set
     * @return the USqlIndex object itself.
     */
    public USqlIndex withIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
        return this;
    }

}
