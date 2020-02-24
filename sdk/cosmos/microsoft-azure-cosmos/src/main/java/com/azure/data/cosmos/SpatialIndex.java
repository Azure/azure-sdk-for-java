// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a spatial index in the Azure Cosmos DB database service.
 */
final class SpatialIndex extends Index {

    /**
     * Initializes a new instance of the SpatialIndex class.
     * <p>
     * Here is an example to instantiate SpatialIndex class passing in the DataType
     * <pre>
     * {@code
     *
     * SpatialIndex spatialIndex = new SpatialIndex(DataType.POINT);
     *
     * }
     * </pre>
     *
     * @param dataType specifies the target data type for the index path specification.
     */
    SpatialIndex(DataType dataType) {
        super(IndexKind.SPATIAL);
        this.dataType(dataType);
    }

    /**
     * Initializes a new instance of the SpatialIndex class.
     *
     * @param jsonString the json string that represents the index.
     */
    SpatialIndex(String jsonString) {
        super(jsonString, IndexKind.SPATIAL);
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
     * @return the SpatialIndex.
     */
    public SpatialIndex dataType(DataType dataType) {
        super.set(Constants.Properties.DATA_TYPE, dataType.toString());
        return this;
    }
}
