// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;

import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Vector Indexes spec for Azure CosmosDB service.
 */
public final class CosmosVectorIndexSpec {

    private final JsonSerializable jsonSerializable;
    private String type;
    private Integer quantizationByteSize;
    private Integer indexingSearchListSize;
    private List<String> vectorIndexShardKey;

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
        this.jsonSerializable.set(Constants.Properties.PATH, path, CosmosItemSerializer.DEFAULT_SERIALIZER);
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
        this.type = type;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_TYPE, this.type, CosmosItemSerializer.DEFAULT_SERIALIZER);

        // Reset restricted properties if the type is not DiskANN or QuantizedFlat
        if (!type.equals(CosmosVectorIndexType.DISK_ANN.toString()) && !type.equals(CosmosVectorIndexType.QUANTIZED_FLAT.toString())) {
            this.quantizationByteSize = null;
            this.vectorIndexShardKey = null;
        }
        if (!type.equals(CosmosVectorIndexType.DISK_ANN.toString())) {
            this.indexingSearchListSize = null;
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
    public Integer getQuantizationByteSize() {
        if (this.quantizationByteSize == null && validateIndexType(false)) {
            this.quantizationByteSize = this.jsonSerializable.getInt(Constants.Properties.VECTOR_QUANTIZATION_BYTE_SIZE);

            if (this.quantizationByteSize <= 0) {
                this.quantizationByteSize = null;
            }
        }
        return this.quantizationByteSize;
    }

    private Boolean validateIndexType(boolean isIndexingSearchListSize) {
        if (this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE).equals(CosmosVectorIndexType.DISK_ANN.toString()) ||
            this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE).equals(CosmosVectorIndexType.DISK_ANN.toString())) {
            return true;
        }

        if (isIndexingSearchListSize && this.jsonSerializable.getString(Constants.Properties.VECTOR_INDEX_TYPE).equals(CosmosVectorIndexType.DISK_ANN.toString())) {
            return true;
        }
        return false;
    }

    /**
     * Sets the quantization byte size
     *
     * @param quantizationByteSize the number of bytes used in product quantization of the vectors. A larger value may
     *                             result in better recall for vector searches at the expense of latency. This applies
     *                             to index types DiskANN and quantizedFlat. The allowed range for this parameter is
     *                             between 1 and 3.
     * @return CosmosVectorIndexSpec
     */
    public CosmosVectorIndexSpec setQuantizationByteSize(Integer quantizationByteSize) {
        this.quantizationByteSize = quantizationByteSize;
        this.jsonSerializable.set(Constants.Properties.VECTOR_QUANTIZATION_BYTE_SIZE, this.quantizationByteSize, CosmosItemSerializer.DEFAULT_SERIALIZER);
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
        if (this.indexingSearchListSize == null && validateIndexType(true)) {
            this.indexingSearchListSize = this.jsonSerializable.getInt(Constants.Properties.VECTOR_INDEXING_SEARCH_LIST_SIZE);

            if (this.indexingSearchListSize <= 0) {
                this.indexingSearchListSize = null;
            }
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
    public CosmosVectorIndexSpec setIndexingSearchListSize(int indexingSearchListSize) {
        this.indexingSearchListSize = indexingSearchListSize;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEXING_SEARCH_LIST_SIZE, this.indexingSearchListSize, CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    /**
     * Gets the vector index shard key
     *
     * @return vectorIndexShardKey the list of string containing the shard keys used for partitioning the vector
     * indexes. This applies to index types DiskANN and quantizedFlat.
     */
    public List<String> getVectorIndexShardKey() {
        if (this.vectorIndexShardKey == null && validateIndexType(false)) {
            this.vectorIndexShardKey = this.jsonSerializable.getList(Constants.Properties.VECTOR_INDEX_SHARD_KEY, String.class);

            if (this.vectorIndexShardKey == null) {
                this.vectorIndexShardKey = new ArrayList<>();
            }
        }
        return this.vectorIndexShardKey;
    }

    /**
     * Sets the vector index shard key
     *
     * @param vectorIndexShardKey the list of string containing the shard keys used for partitioning the vector
     *                            indexes. This applies to index types DiskANN and quantizedFlat.
     * @return CosmosVectorIndexSpec
     */
    public CosmosVectorIndexSpec setVectorIndexShardKey(List<String> vectorIndexShardKey) {
        this.vectorIndexShardKey = vectorIndexShardKey;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_SHARD_KEY, this.indexingSearchListSize, CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }
}
