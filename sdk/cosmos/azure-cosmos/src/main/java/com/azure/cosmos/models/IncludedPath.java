// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HashIndex;
import com.azure.cosmos.implementation.Index;
import com.azure.cosmos.implementation.IndexKind;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.RangeIndex;
import com.azure.cosmos.implementation.SpatialIndex;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an included path of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public final class IncludedPath {
    private List<Index> indexes;
    private JsonSerializable jsonSerializable;

    /**
     * Constructor.
     *
     * @param path the included path.
     */
    public IncludedPath(String path) {
        this.jsonSerializable = new JsonSerializable();
        this.setPath(path);
    }

    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the included path.
     */
    IncludedPath(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
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
     * @return the Included Path.
     */
    public IncludedPath setPath(String path) {
        this.jsonSerializable.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the paths that are chosen to be indexed by the user.
     *
     * @return the included paths.
     */
    List<Index> getIndexes() {
        if (this.indexes == null) {
            this.indexes = this.getIndexCollection();

            if (this.indexes == null) {
                this.indexes = new ArrayList<Index>();
            }
        }

        return this.indexes;
    }

    /**
     * Sets indexes.
     *
     * @param indexes the indexes
     * @return the indexes
     */
    IncludedPath setIndexes(List<Index> indexes) {
        this.indexes = indexes;
        return this;
    }

    private List<Index> getIndexCollection() {
        if (this.jsonSerializable.getPropertyBag() != null && this.jsonSerializable.getPropertyBag().has(Constants.Properties.INDEXES)) {
            ArrayNode jsonArray = (ArrayNode) this.jsonSerializable.getPropertyBag().get(Constants.Properties.INDEXES);
            List<Index> result = new ArrayList<Index>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonNode jsonObject = jsonArray.get(i);

                IndexKind indexKind = IndexKind.valueOf(StringUtils.upperCase(
                    jsonObject.get(Constants.Properties.INDEX_KIND).asText()));
                switch (indexKind) {
                    case HASH:
                        result.add(new HashIndex(jsonObject.toString()));
                        break;
                    case RANGE:
                        result.add(new RangeIndex(jsonObject.toString()));
                        break;
                    case SPATIAL:
                        result.add(new SpatialIndex(jsonObject.toString()));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + indexKind);
                }
            }

            return result;
        }

        return null;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        if (this.indexes != null) {
            for (Index index : this.indexes) {
                index.populatePropertyBag();
            }

            this.jsonSerializable.set(Constants.Properties.INDEXES, this.indexes);
        }
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        IncludedPath that = (IncludedPath) o;
        return Objects.equals(jsonSerializable, that.jsonSerializable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsonSerializable);
    }
}
