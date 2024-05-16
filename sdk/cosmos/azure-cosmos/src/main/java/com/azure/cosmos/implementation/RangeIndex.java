// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a range index in the Azure Cosmos DB database service.
 */
public final class RangeIndex extends Index {

    /**
     * Initializes a new instance of the RangeIndex class with specified DataType.
     * <p>
     * Here is an example to instantiate RangeIndex class passing in the DataType:
     * {@code RangeIndex rangeIndex = new RangeIndex(DataType.NUMBER);}
     *
     * @param dataType the data type.
     */
    RangeIndex(DataType dataType) {
        super(IndexKind.RANGE);
        this.setDataType(dataType);
    }

    /**
     * Initializes a new instance of the RangeIndex class with specified DataType and precision.
     * {@code RangeIndex rangeIndex = new RangeIndex(DataType.NUMBER, -1);}
     *
     * @param dataType the data type of the RangeIndex
     * @param precision the precision of the RangeIndex
     */
    RangeIndex(DataType dataType, int precision) {
        super(IndexKind.RANGE);
        this.setDataType(dataType);
        this.setPrecision(precision);
    }

    /**
     * Initializes a new instance of the RangeIndex class with json string.
     *
     * @param jsonString the json string that represents the index.
     */
    public RangeIndex(String jsonString) {
        super(jsonString, IndexKind.RANGE);
        if (this.getDataType() == null) {
            throw new IllegalArgumentException("The jsonString doesn't contain a valid 'dataType'.");
        }
    }

    /**
     * Initializes a new instance of the RangeIndex class with json string.
     *
     * @param objectNode the object node that represents the index.
     */
    RangeIndex(ObjectNode objectNode) {
        super(objectNode, IndexKind.RANGE);
        if (this.getDataType() == null) {
            throw new IllegalArgumentException("The jsonString doesn't contain a valid 'dataType'.");
        }
    }

    /**
     * Gets data type.
     *
     * @return the data type.
     */
    public DataType getDataType() {
        DataType result = null;
        try {
            result = DataType.valueOf(StringUtils.upperCase(super.getString(Constants.Properties.DATA_TYPE)));
        } catch (IllegalArgumentException e) {
            super.getLogger().warn("INVALID index dataType value {}.", super.getString(Constants.Properties.DATA_TYPE));
        }
        return result;
    }

    /**
     * Sets data type.
     *
     * @param dataType the data type.
     * @return the RangeIndex.
     */
    public RangeIndex setDataType(DataType dataType) {
        super.set(Constants.Properties.DATA_TYPE, dataType.toString(), CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    /**
     * Gets precision.
     *
     * @return the precision.
     */
    public int getPrecision() {
        return super.getInt(Constants.Properties.PRECISION);
    }

    /**
     * Sets precision.
     *
     * @param precision the precision.
     * @return the RangeIndex.
     */
    public RangeIndex setPrecision(int precision) {
        super.set(Constants.Properties.PRECISION, precision, CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    boolean hasPrecision() {
        return super.has(Constants.Properties.PRECISION);
    }
}
