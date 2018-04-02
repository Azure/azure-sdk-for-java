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
 * Represents the index of a collection in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public abstract class Index extends JsonSerializable {

    /**
     * Constructor.
     *
     * @param indexKind the kind of the index
     */
    protected Index(IndexKind indexKind) {
        super();
        this.setKind(indexKind);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the index.
     * @param indexKind the kind of the index
     */
    protected Index(String jsonString, IndexKind indexKind) {
        super(jsonString);
        this.setKind(indexKind);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the index.
     * @param indexKind the kind of the index
     */
    protected Index(JSONObject jsonObject, IndexKind indexKind) {
        super(jsonObject);
        this.setKind(indexKind);
    }

    /**
     * Returns an instance of RangeIndex class with specified DataType.
     * <p>
     * Here is an example to create RangeIndex instance passing in the DataType:
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = Index.Range(DataType.Number);
     *
     * }
     * </pre>
     *
     * @param dataType the data type.
     * @return an instance of RangeIndex type.
     */
    public static RangeIndex Range(DataType dataType) {
        return new RangeIndex(dataType);
    }

    /**
     * Returns an instance of RangeIndex class with specified DataType and precision.
     * <p>
     * Here is an example to create RangeIndex instance passing in the DataType and precision:
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = Index.Range(DataType.Number, -1);
     *
     * }
     * </pre>
     *
     * @param dataType  specifies the target data type for the index path specification.
     * @param precision specifies the precision to be used for the data type associated with this index.
     * @return an instance of RangeIndex type.
     */
    public static RangeIndex Range(DataType dataType, int precision) {
        return new RangeIndex(dataType, precision);
    }

    /**
     * Returns an instance of HashIndex class with specified DataType.
     * <p>
     * Here is an example to create HashIndex instance passing in the DataType:
     * <pre>
     * {@code
     *
     * HashIndex hashIndex = Index.Hash(DataType.String);
     * }
     * </pre>
     *
     * @param dataType specifies the target data type for the index path specification.
     * @return an instance of HashIndex type.
     */
    public static HashIndex Hash(DataType dataType) {
        return new HashIndex(dataType);
    }

    /**
     * Returns an instance of HashIndex class with specified DataType and precision.
     * <p>
     * Here is an example to create HashIndex instance passing in the DataType and precision:
     * <p>
     * HashIndex hashIndex = Index.Hash(DataType.String, 3);
     *
     * @param dataType  specifies the target data type for the index path specification.
     * @param precision specifies the precision to be used for the data type associated with this index.
     * @return an instance of HashIndex type.
     */
    public static HashIndex Hash(DataType dataType, int precision) {
        return new HashIndex(dataType, precision);
    }

    /**
     * Returns an instance of SpatialIndex class with specified DataType.
     * <p>
     * Here is an example to create SpatialIndex instance passing in the DataType:
     * <p>
     * SpatialIndex spatialIndex = Index.Spatial(DataType.Point);
     *
     * @param dataType specifies the target data type for the index path specification.
     * @return an instance of SpatialIndex type.
     */
    public static SpatialIndex Spatial(DataType dataType) {
        return new SpatialIndex(dataType);
    }

    /**
     * Gets index kind.
     *
     * @return the index kind.
     */
    public IndexKind getKind() {
        IndexKind result = null;
        try {
            result = IndexKind.valueOf(WordUtils.capitalize(super.getString(Constants.Properties.INDEX_KIND)));
        } catch (IllegalArgumentException e) {
            this.getLogger().warn("Invalid index kind value %s.", super.getString(Constants.Properties.INDEX_KIND));
        }

        return result;
    }

    /**
     * Sets index kind.
     *
     * @param indexKind the index kind.
     */
    private void setKind(IndexKind indexKind) {
        super.set(Constants.Properties.INDEX_KIND, indexKind.name());
    }
}
