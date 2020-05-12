// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.IndexKind;

/**
 * Represents a hash index in the Azure Cosmos DB database service.
 */
public final class HashIndex extends Index {

    /**
     * Specifies an instance of HashIndex class with specified DataType.
     * <p>
     * Here is an example to instantiate HashIndex class passing in the DataType:
     * <p>
     * {@code HashIndex hashIndex = new HashIndex(DataType.STRING);}
     *
     * @param dataType the data type.
     */
    HashIndex(DataType dataType) {
        super(IndexKind.HASH);
        this.setDataType(dataType);
    }

    /**
     * Initializes a new instance of the HashIndex class with specified DataType and precision.
     * <p>
     * Here is an example to instantiate HashIndex class passing in the DataType:
     * {@code HashIndex hashIndex = new HashIndex(DataType.STRING, 3);}
     *
     * @param dataType the data type.
     * @param precision the precision.
     */
    HashIndex(DataType dataType, int precision) {
        super(IndexKind.HASH);
        this.setDataType(dataType);
        this.setPrecision(precision);
    }

    /**
     * Initializes a new instance of the HashIndex class with json string.
     *
     * @param jsonString the json string that represents the index.
     */
    HashIndex(String jsonString) {
        super(jsonString, IndexKind.HASH);
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
            result = DataType.valueOf(StringUtils.upperCase(this.jsonSerializable.getString(Constants.Properties.DATA_TYPE)));
        } catch (IllegalArgumentException e) {
            // Ignore exception and let the caller handle null value.
            this.jsonSerializable.getLogger().warn("INVALID index dataType value {}.",
                this.jsonSerializable.getString(Constants.Properties.DATA_TYPE));
        }
        return result;
    }

    /**
     * Sets data type.
     *
     * @param dataType the data type.
     * @return the Hash Index.
     */
    public HashIndex setDataType(DataType dataType) {
        this.jsonSerializable.set(Constants.Properties.DATA_TYPE, dataType.toString());
        return this;
    }

    /**
     * Gets precision.
     *
     * @return the precision.
     */
    public int getPrecision() {
        return this.jsonSerializable.getInt(Constants.Properties.PRECISION);
    }

    /**
     * Sets precision.
     *
     * @param precision the precision.
     * @return the Hash Index.
     */
    public HashIndex setPrecision(int precision) {
        this.jsonSerializable.set(Constants.Properties.PRECISION, precision);
        return this;
    }

    boolean hasPrecision() {
        return this.jsonSerializable.has(Constants.Properties.PRECISION);
    }
}
