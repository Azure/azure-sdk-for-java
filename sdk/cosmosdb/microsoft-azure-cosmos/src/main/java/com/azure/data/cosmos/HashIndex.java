// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a hash index in the Azure Cosmos DB database service.
 */
public final class HashIndex extends Index {

    /**
     * Specifies an instance of HashIndex class with specified DataType.
     * <p>
     * Here is an example to instantiate HashIndex class passing in the DataType:
     * <pre>
     * {@code
     *
     * HashIndex hashIndex = new HashIndex(DataType.STRING);
     *
     * }
     * </pre>
     *
     * @param dataType the data type.
     */
    public HashIndex(DataType dataType) {
        super(IndexKind.HASH);
        this.dataType(dataType);
    }

    /**
     * Initializes a new instance of the HashIndex class with specified DataType and precision.
     * <p>
     * Here is an example to instantiate HashIndex class passing in the DataType:
     * <pre>
     * {@code
     *
     * HashIndex hashIndex = new HashIndex(DataType.STRING, 3);
     *
     * }
     * </pre>
     *
     * @param dataType  the data type.
     * @param precision the precision.
     */
    public HashIndex(DataType dataType, int precision) {
        super(IndexKind.HASH);
        this.dataType(dataType);
        this.precision(precision);
    }

    /**
     * Initializes a new instance of the HashIndex class with json string.
     *
     * @param jsonString the json string that represents the index.
     */
    HashIndex(String jsonString) {
        super(jsonString, IndexKind.HASH);
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
            // Ignore exception and let the caller handle null value.
            this.getLogger().warn("INVALID index dataType value {}.", super.getString(Constants.Properties.DATA_TYPE));
        }
        return result;
    }

    /**
     * Sets data type.
     *
     * @param dataType the data type.
     * @return the Hash Index.
     */
    public HashIndex dataType(DataType dataType) {
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
     * @return the Hash Index.
     */
    public HashIndex precision(int precision) {
        super.set(Constants.Properties.PRECISION, precision);
        return this;
    }

    boolean hasPrecision() {
        return super.has(Constants.Properties.PRECISION);
    }
}
