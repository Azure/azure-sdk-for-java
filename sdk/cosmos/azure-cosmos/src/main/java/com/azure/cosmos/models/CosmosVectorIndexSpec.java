// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.query.IndexProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Vector Indexes spec for Azure CosmosDB service.
 */
public final class CosmosVectorIndexSpec {

    private String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(Constants.Properties.VECTOR_QUANTIZATION_SIZE_IN_BYTES)
    private Integer quantizationSizeInBytes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer indexingSearchListSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> vectorIndexShardKeys;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private QuantizerType quantizerType;
    private final JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosVectorIndexSpec() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String getPath() {
        return this.jsonSerializable.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the SpatialSpec.
     */
    public CosmosVectorIndexSpec setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the vector index type for the vector index
     *
     * @return the vector index type
     */
    public String getType() {
        if (this.type == null) {
            this.type = this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE);
        }
        return this.type;
    }

    /**
     * Sets the vector index type for the vector index
     *
     * @param type the vector index type
     * @return the VectorIndexSpec
     */
    public CosmosVectorIndexSpec setType(String type) {
        checkNotNull(type, "cosmosVectorIndexType cannot be null");
        if (!CosmosVectorIndexType.isValidType(type)) {
            throw new IllegalArgumentException(String.format("%s is an invalid index type. Valid index types are 'flat', 'quantizedFlat' or 'diskANN'.", type));
        }
        this.type = type;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_TYPE, this.type);

        return this;
    }

    /**
     * Gets quantizer type.
     *
     * @return the quantizer type.
     */
    public QuantizerType getQuantizerType() {
        if (this.quantizerType == null) {
            this.quantizerType = this.jsonSerializable.getObject(Constants.Properties.QUANTIZER_TYPE, QuantizerType.class);
        }
        return this.quantizerType;
    }

    /**
     * Set quantizer type.
     *
     * @param quantizerType The quantizer type
     * @return the SpatialSpec.
     */
    public CosmosVectorIndexSpec setQuantizerType(QuantizerType quantizerType) {
        if (validateIndexType(IndexProperty.QUANTIZER_TYPE) && quantizerType != null) {
            this.quantizerType = quantizerType;
            this.jsonSerializable.set(Constants.Properties.QUANTIZER_TYPE, quantizerType);
        } else {
            this.quantizerType = null;
        }
        return this;
    }

    /**
     * Gets the quantization byte size
     *
     * @return quantizationByteSize the number of bytes used in product quantization of the vectors.
     * A larger value may result in better recall for vector searches at the expense of latency.
     * This applies to index types DiskANN and quantizedFlat. The allowed range for this parameter
     * is between 1 and 3.
     */
    public Integer getQuantizationSizeInBytes() {
        if (this.quantizationSizeInBytes == null) {
            this.quantizationSizeInBytes = this.jsonSerializable.getInt(Constants.Properties.VECTOR_QUANTIZATION_SIZE_IN_BYTES);
        }
        return this.quantizationSizeInBytes;
    }

    /**
     * Sets the quantization byte size
     *
     * @param quantizationByteSize the number of bytes used in product quantization of the vectors. A larger value may
     *                             result in better recall for vector searches at the expense of latency. This applies
     *                             to index types DiskANN and quantizedFlat. The allowed range for this parameter is
     *                             between 1 and min(Dimensions, 512). The default value would be 64.
     * @return CosmosVectorIndexSpec
     */
    public CosmosVectorIndexSpec setQuantizationSizeInBytes(Integer quantizationByteSize) {
        if (validateIndexType(IndexProperty.QUANTIZATION_SIZE_IN_BYTES) && quantizationByteSize != null) {
            this.quantizationSizeInBytes = quantizationByteSize;
            this.jsonSerializable.set(Constants.Properties.VECTOR_QUANTIZATION_SIZE_IN_BYTES, this.quantizationSizeInBytes);
        } else {
            this.quantizationSizeInBytes = null;
        }
        return this;
    }

    /**
     * Gets the indexing search list size
     *
     * @return indexingSearchListSize which represents the size of the candidate list of approximate neighbors stored
     * while building the DiskANN index as part of the optimization processes. The allowed range for this
     * parameter is between 25 and 500.
     */
    public Integer getIndexingSearchListSize() {
        if (this.indexingSearchListSize == null) {
            this.indexingSearchListSize = this.jsonSerializable.getInt(Constants.Properties.VECTOR_INDEXING_SEARCH_LIST_SIZE);
        }
        return this.indexingSearchListSize;
    }

    /**
     * Sets the indexing search list size
     *
     * @param indexingSearchListSize indexingSearchListSize which represents the size of the candidate list of
     *                               approximate neighbors stored while building the DiskANN index as part of
     *                               the optimization processes. The allowed range for this parameter is between
     *                               25 and 500.
     * @return CosmosVectorIndexSpec
     */
    public CosmosVectorIndexSpec setIndexingSearchListSize(Integer indexingSearchListSize) {
        if (validateIndexType(IndexProperty.INDEXING_SEARCH_LIST_SIZE) && indexingSearchListSize != null) {
            this.indexingSearchListSize = indexingSearchListSize;
            this.jsonSerializable.set(Constants.Properties.VECTOR_INDEXING_SEARCH_LIST_SIZE, this.indexingSearchListSize);
        } else {
            this.indexingSearchListSize = null;
        }
        return this;
    }

    /**
     * Gets the vector indexing shard keys
     *
     * @return vectorIndexShardKeys the list of string containing the shard keys used for partitioning the
     * vector indexes. This applies to index types diskANN and quantizedFlat. The maximum allowed size for
     * this array is currently limited to 1 - that is, there is only one allowed path.
     */
    public List<String> getVectorIndexShardKeys() {
        if (this.vectorIndexShardKeys == null) {
            this.vectorIndexShardKeys = this.jsonSerializable.getList(Constants.Properties.VECTOR_INDEX_SHARD_KEYS, String.class);
        }
        return this.vectorIndexShardKeys;
    }

    /**
     * Sets the vector indexing shard keys
     *
     * @param vectorIndexShardKeys vectorIndexShardKeys the list of string containing the shard keys used for partitioning the
     *                            vector indexes. This applies to index types diskANN and quantizedFlat. The maximum allowed size for
     *                            this array is currently limited to 1 - that is, there is only one allowed path.
     * @return CosmosVectorIndexSpec
     */
    public CosmosVectorIndexSpec setVectorIndexShardKeys(List<String> vectorIndexShardKeys) {
        if (validateIndexType(IndexProperty.VECTOR_INDEX_SHARD_KEYS) && vectorIndexShardKeys != null) {
            this.vectorIndexShardKeys = vectorIndexShardKeys;
            this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_SHARD_KEYS, this.vectorIndexShardKeys);
        } else {
            this.vectorIndexShardKeys = null;
        }
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }

    private Boolean validateIndexType(IndexProperty indexProperty) {
        String vectorIndexType = this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE);
        if (indexProperty.equals(IndexProperty.QUANTIZATION_SIZE_IN_BYTES) ||
            (indexProperty.equals(IndexProperty.VECTOR_INDEX_SHARD_KEYS)) ||
            (indexProperty.equals(IndexProperty.QUANTIZER_TYPE))) {
            return vectorIndexType.equals(CosmosVectorIndexType.QUANTIZED_FLAT.toString()) ||
                vectorIndexType.equals(CosmosVectorIndexType.DISK_ANN.toString());
        }
        return vectorIndexType.equals(CosmosVectorIndexType.DISK_ANN.toString());
    }
}
