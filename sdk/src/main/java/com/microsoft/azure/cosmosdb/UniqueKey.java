/*
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents a unique key on that enforces uniqueness constraint on documents in the collection in the Azure Cosmos DB service.
 *
 * 1) For partitioned collections, the value of partition key is implicitly a part of each unique key.
 * 2) Uniqueness constraint is also enforced for missing values.
 * For instance, if unique key policy defines a unique key with single property path, there could be only one document that has missing value for this property.
 * @see UniqueKeyPolicy
 */
@SuppressWarnings("serial")
public class UniqueKey extends JsonSerializable {
    private Collection<String> paths;

    public UniqueKey() {
        super();
    }

    public UniqueKey(String jsonString) {
        super(jsonString);
    }

    public UniqueKey(ObjectNode jsonObject) {
        super(jsonObject);
    }

    /**
     * Gets the paths, a set of which must be unique for each document in the Azure Cosmos DB service.
     *
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the document,
     * such as "/name/first".
     *
     * @return the unique paths.
     */
    public Collection<String> getPaths() {
        if (this.paths == null) {
            this.paths = super.getCollection(Constants.Properties.PATHS, String.class);

            if (this.paths == null) {
                this.paths = new ArrayList<String>();
            }
        }

        return this.paths;
    }


    /**
     * Sets the paths, a set of which must be unique for each document in the Azure Cosmos DB service.
     *
     * The paths to enforce uniqueness on. Each path is a rooted path of the unique property in the document,
     * such as "/name/first".
     *
     * @param paths the unique paths.
     */
    public void setPaths(Collection<String> paths) {
        this.paths = paths;
    }

    @Override
    void populatePropertyBag() {
        if (paths != null) {
            super.set(Constants.Properties.PATHS, paths);
        }
    }
}
