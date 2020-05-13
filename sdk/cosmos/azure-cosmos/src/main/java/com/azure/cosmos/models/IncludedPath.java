// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.IndexKind;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an included path of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public final class IncludedPath extends JsonSerializableWrapper{
    private Collection<Index> indexes;

    /**
     * Constructor.
     */
    public IncludedPath() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    public IncludedPath(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
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
    public Collection<Index> getIndexes() {
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
    public IncludedPath setIndexes(Collection<Index> indexes) {
        this.indexes = indexes;
        return this;
    }

    private Collection<Index> getIndexCollection() {
        if (this.jsonSerializable.getPropertyBag() != null && this.jsonSerializable.getPropertyBag().has(Constants.Properties.INDEXES)) {
            ArrayNode jsonArray = (ArrayNode) this.jsonSerializable.getPropertyBag().get(Constants.Properties.INDEXES);
            Collection<Index> result = new ArrayList<Index>();

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

    @Override
    protected void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        if (this.indexes != null) {
            for (Index index : this.indexes) {
                index.populatePropertyBag();
            }

            this.jsonSerializable.set(Constants.Properties.INDEXES, this.indexes);
        }
    }
}
