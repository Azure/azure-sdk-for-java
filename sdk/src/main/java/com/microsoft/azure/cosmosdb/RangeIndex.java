/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents a range index in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class RangeIndex extends Index {

    /**
     * Initializes a new instance of the RangeIndex class with specified DataType.
     * <p>
     * Here is an example to instantiate RangeIndex class passing in the DataType:
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = new RangeIndex(DataType.Number);
     *
     * }
     * </pre>
     *
     * @param dataType the data type.
     */
    public RangeIndex(DataType dataType) {
        super(IndexKind.Range);
        this.setDataType(dataType);
    }

    /**
     * Initializes a new instance of the RangeIndex class with specified DataType and precision.
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = new RangeIndex(DataType.Number, -1);
     *
     * }
     * </pre>
     * @param dataType   the data type of the RangeIndex
     * @param precision  the precision of the RangeIndex
     */
    public RangeIndex(DataType dataType, int precision) {
        super(IndexKind.Range);
        this.setDataType(dataType);
        this.setPrecision(precision);
    }

    /**
     * Initializes a new instance of the RangeIndex class with json string.
     *
     * @param jsonString the json string that represents the index.
     */
    public RangeIndex(String jsonString) {
        super(jsonString, IndexKind.Range);
        if (this.getDataType() == null) {
            throw new IllegalArgumentException("The jsonString doesn't contain a valid 'dataType'.");
        }
    }

    /**
     * Initializes a new instance of the RangeIndex class with json object.
     *
     * @param jsonObject the json object that represents the index.
     */
    public RangeIndex(JSONObject jsonObject) {
        super(jsonObject, IndexKind.Range);
        if (this.getDataType() == null) {
            throw new IllegalArgumentException("The jsonObject doesn't contain a valid 'dataType'.");
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
            result = DataType.valueOf(WordUtils.capitalize(super.getString(Constants.Properties.DATA_TYPE)));
        } catch (IllegalArgumentException e) {
            this.getLogger().warn("Invalid index dataType value {}.", super.getString(Constants.Properties.DATA_TYPE));
        }
        return result;
    }

    /**
     * Sets data type.
     *
     * @param dataType the data type.
     */
    public void setDataType(DataType dataType) {
        super.set(Constants.Properties.DATA_TYPE, dataType.name());
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
     */
    public void setPrecision(int precision) {
        super.set(Constants.Properties.PRECISION, precision);
    }

    boolean hasPrecision() {
        return super.has(Constants.Properties.PRECISION);
    }
}
