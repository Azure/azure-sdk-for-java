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

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents an included path of the IndexingPolicy in the Azure Cosmos DB database service.
 */
public class IncludedPath extends JsonSerializable {

    private Collection<Index> indexes;

    /**
     * Constructor.
     */
    public IncludedPath() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the included path.
     */
    public IncludedPath(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets path.
     *
     * @return the path.
     */
    public String path() {
        return super.getString(Constants.Properties.PATH);
    }

    /**
     * Sets path.
     *
     * @param path the path.
     * @return the Included Path.
     */
    public IncludedPath path(String path) {
        super.set(Constants.Properties.PATH, path);
        return this;
    }

    /**
     * Gets the paths that are chosen to be indexed by the user.
     *
     * @return the included paths.
     */
    public Collection<Index> indexes() {
        if (this.indexes == null) {
            this.indexes = this.indexCollection();

            if (this.indexes == null) {
                this.indexes = new ArrayList<Index>();
            }
        }

        return this.indexes;
    }

    public IncludedPath indexes(Collection<Index> indexes) {
        this.indexes = indexes;
        return this;
    }

    private Collection<Index> indexCollection() {
        if (this.propertyBag != null && this.propertyBag.has(Constants.Properties.INDEXES)) {
            ArrayNode jsonArray = (ArrayNode) this.propertyBag.get(Constants.Properties.INDEXES);
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
                }
            }

            return result;
        }

        return null;
    }

    @Override
    void populatePropertyBag() {
        if (this.indexes != null) {
            for (Index index : this.indexes) {
                index.populatePropertyBag();
            }

            super.set(Constants.Properties.INDEXES, this.indexes);
        }
    }
}
