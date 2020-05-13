// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.IndexKind;

/**
 * Represents the index of a collection in the Azure Cosmos DB database service.
 */
public abstract class Index extends JsonSerializableWrapper{

    /**
     * Constructor.
     *
     * @param indexKind the kind of the index
     */
    Index(IndexKind indexKind) {
        this.jsonSerializable = new JsonSerializable();
        this.setKind(indexKind);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the index.
     * @param indexKind the kind of the index
     */
    Index(String jsonString, IndexKind indexKind) {
        this.jsonSerializable = new JsonSerializable(jsonString);
        this.setKind(indexKind);
    }

    /**
     * Returns an instance of RangeIndex class with specified DataType.
     * <p>
     * Here is an example to create RangeIndex instance passing in the DataType:
     * {@code RangeIndex rangeIndex = Index.range(DataType.NUMBER); }
     *
     * @param dataType the data type.
     * @return an instance of RangeIndex type.
     */
    public static RangeIndex range(DataType dataType) {
        return new RangeIndex(dataType);
    }

    /**
     * Returns an instance of RangeIndex class with specified DataType and precision.
     * <p>
     * Here is an example to create RangeIndex instance passing in the DataType and precision:
     * {@code RangeIndex rangeIndex = Index.RANGE(DataType.NUMBER, -1);}
     *
     * @param dataType specifies the target data type for the index path specification.
     * @param precision specifies the precision to be used for the data type associated with this index.
     * @return an instance of RangeIndex type.
     */
    public static RangeIndex range(DataType dataType, int precision) {
        return new RangeIndex(dataType, precision);
    }

    /**
     * Returns an instance of HashIndex class with specified DataType.
     * <p>
     * Here is an example to create HashIndex instance passing in the DataType:
     *
     * {@code  HashIndex hashIndex = Index.HASH(DataType.STRING);}
     *
     * @param dataType specifies the target data type for the index path specification.
     * @return an instance of HashIndex type.
     */
    public static HashIndex hash(DataType dataType) {
        return new HashIndex(dataType);
    }

    /**
     * Returns an instance of HashIndex class with specified DataType and precision.
     * <p>
     * Here is an example to create HashIndex instance passing in the DataType and precision:
     * <p>
     * HashIndex hashIndex = Index.HASH(DataType.STRING, 3);
     *
     * @param dataType specifies the target data type for the index path specification.
     * @param precision specifies the precision to be used for the data type associated with this index.
     * @return an instance of HashIndex type.
     */
    public static HashIndex hash(DataType dataType, int precision) {
        return new HashIndex(dataType, precision);
    }

    /**
     * Returns an instance of SpatialIndex class with specified DataType.
     * <p>
     * Here is an example to create SpatialIndex instance passing in the DataType:
     * <p>
     * SpatialIndex spatialIndex = Index.SPATIAL(DataType.POINT);
     *
     * @param dataType specifies the target data type for the index path specification.
     * @return an instance of SpatialIndex type.
     */
    public static SpatialIndex spatial(DataType dataType) {
        return new SpatialIndex(dataType);
    }

    /**
     * Gets index kind.
     *
     * @return the index kind.
     */
    IndexKind getKind() {
        IndexKind result = null;
        try {
            result = IndexKind.valueOf(StringUtils.upperCase(this.jsonSerializable.getString(Constants.Properties.INDEX_KIND)));
        } catch (IllegalArgumentException e) {
            this.jsonSerializable.getLogger().warn("INVALID index kind value %s.", this.jsonSerializable.getString(Constants.Properties.INDEX_KIND));
        }

        return result;
    }

    /**
     * Sets index kind.
     *
     * @param indexKind the index kind.
     */
    private Index setKind(IndexKind indexKind) {
        this.jsonSerializable.set(Constants.Properties.INDEX_KIND, indexKind.toString());
        return this;
    }
}
