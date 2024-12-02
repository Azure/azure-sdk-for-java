// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.query.IndexProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        if (!CosmosVectorIndexType.isValidType(type)) {
            throw new IllegalArgumentException(String.format("%s is an invalid index type. Valid index types are 'flat', 'quantizedFlat' or 'diskANN'.", type));
        }
        this.type = type;
        this.jsonSerializable.set(Constants.Properties.VECTOR_INDEX_TYPE, this.type, CosmosItemSerializer.DEFAULT_SERIALIZER);

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
            this.jsonSerializable.set(Constants.Properties.VECTOR_QUANTIZATION_SIZE_IN_BYTES, this.quantizationSizeInBytes, CosmosItemSerializer.DEFAULT_SERIALIZER);
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
            this.jsonSerializable.set(Constants.Properties.VECTOR_INDEXING_SEARCH_LIST_SIZE, this.indexingSearchListSize, CosmosItemSerializer.DEFAULT_SERIALIZER);
        } else {
            this.indexingSearchListSize = null;
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
        if (indexProperty.equals(IndexProperty.QUANTIZATION_SIZE_IN_BYTES)) {
            return vectorIndexType.equals(CosmosVectorIndexType.QUANTIZED_FLAT.toString()) ||
                vectorIndexType.equals(CosmosVectorIndexType.DISK_ANN.toString());
        }
        return vectorIndexType.equals(CosmosVectorIndexType.DISK_ANN.toString());
    }
}
