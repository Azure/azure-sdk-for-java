// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a range index in the Azure Cosmos DB database service.
 */
public final class RangeIndex extends Index {

    /**
     * Initializes a new instance of the RangeIndex class with specified DataType.
     * <p>
     * Here is an example to instantiate RangeIndex class passing in the DataType:
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = new RangeIndex(DataType.NUMBER);
     *
     * }
     * </pre>
     *
     * @param dataType the data type.
     */
    public RangeIndex(DataType dataType) {
        super(IndexKind.RANGE);
        this.dataType(dataType);
    }

    /**
     * Initializes a new instance of the RangeIndex class with specified DataType and precision.
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = new RangeIndex(DataType.NUMBER, -1);
     *
     * }
     * </pre>
     * @param dataType   the data type of the RangeIndex
     * @param precision  the precision of the RangeIndex
     */
    public RangeIndex(DataType dataType, int precision) {
        super(IndexKind.RANGE);
        this.dataType(dataType);
        this.precision(precision);
    }

    /**
     * Initializes a new instance of the RangeIndex class with json string.
     *
     * @param jsonString the json string that represents the index.
     */
    RangeIndex(String jsonString) {
        super(jsonString, IndexKind.RANGE);
        if (this.dataType() == null) {
            throw new IllegalArgumentException("The jsonString doesn't contain a valid 'dataType'.");
        }
    }

    /**
     * Gets data type.
     *
     * @return the data type.
     */
    public DataType dataType() {
        DataType result = null;
        try {
            result = DataType.valueOf(StringUtils.upperCase(super.getString(Constants.Properties.DATA_TYPE)));
        } catch (IllegalArgumentException e) {
            this.getLogger().warn("INVALID index dataType value {}.", super.getString(Constants.Properties.DATA_TYPE));
        }
        return result;
    }

    /**
     * Sets data type.
     *
     * @param dataType the data type.
     * @return the RangeIndex.
     */
    public RangeIndex dataType(DataType dataType) {
        super.set(Constants.Properties.DATA_TYPE, dataType.toString());
        return this;
    }

    /**
     * Gets precision.
     *
     * @return the precision.
     */
    public int precision() {
        return super.getInt(Constants.Properties.PRECISION);
    }

    /**
     * Sets precision.
     *
     * @param precision the precision.
     * @return the RangeIndex.
     */
    public RangeIndex precision(int precision) {
        super.set(Constants.Properties.PRECISION, precision);
        return this;
    }

    boolean hasPrecision() {
        return super.has(Constants.Properties.PRECISION);
    }
}
