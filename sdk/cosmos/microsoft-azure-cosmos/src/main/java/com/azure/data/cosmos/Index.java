// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents the index of a collection in the Azure Cosmos DB database service.
 */
public abstract class Index extends JsonSerializable {

    /**
     * Constructor.
     *
     * @param indexKind the kind of the index
     */
    Index(IndexKind indexKind) {
        super();
        this.kind(indexKind);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the index.
     * @param indexKind the kind of the index
     */
    Index(String jsonString, IndexKind indexKind) {
        super(jsonString);
        this.kind(indexKind);
    }

    /**
     * Returns an instance of RangeIndex class with specified DataType.
     * <p>
     * Here is an example to create RangeIndex instance passing in the DataType:
     * <pre>
     * {@code
     *
     * RangeIndex rangeIndex = Index.RANGE(DataType.NUMBER);
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
     * RangeIndex rangeIndex = Index.RANGE(DataType.NUMBER, -1);
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
     * HashIndex hashIndex = Index.HASH(DataType.STRING);
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
     * HashIndex hashIndex = Index.HASH(DataType.STRING, 3);
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
     * SpatialIndex spatialIndex = Index.SPATIAL(DataType.POINT);
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
    public IndexKind kind() {
        IndexKind result = null;
        try {
            result = IndexKind.valueOf(StringUtils.upperCase(super.getString(Constants.Properties.INDEX_KIND)));
        } catch (IllegalArgumentException e) {
            this.getLogger().warn("INVALID index kind value %s.", super.getString(Constants.Properties.INDEX_KIND));
        }

        return result;
    }

    /**
     * Sets index kind.
     *
     * @param indexKind the index kind.
     */
    private Index kind(IndexKind indexKind) {
        super.set(Constants.Properties.INDEX_KIND, indexKind.toString());
        return this;
    }
}
