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
 * Represents a spatial index in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class SpatialIndex extends Index {

    /**
     * Initializes a new instance of the SpatialIndex class.
     * <p>
     * Here is an example to instantiate SpatialIndex class passing in the DataType
     * <pre>
     * {@code
     *
     * SpatialIndex spatialIndex = new SpatialIndex(DataType.Point);
     *
     * }
     * </pre>
     *
     * @param dataType specifies the target data type for the index path specification.
     */
    public SpatialIndex(DataType dataType) {
        super(IndexKind.Spatial);
        this.setDataType(dataType);
    }

    /**
     * Initializes a new instance of the SpatialIndex class.
     *
     * @param jsonString the json string that represents the index.
     */
    public SpatialIndex(String jsonString) {
        super(jsonString, IndexKind.Spatial);
        if (this.getDataType() == null) {
            throw new IllegalArgumentException("The jsonString doesn't contain a valid 'dataType'.");
        }
    }

    /**
     * Initializes a new instance of the SpatialIndex class.
     *
     * @param jsonObject the json object that represents the index.
     */
    public SpatialIndex(JSONObject jsonObject) {
        super(jsonObject, IndexKind.Spatial);
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
}
