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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents the unique key policy configuration for specifying uniqueness constraints on documents in the
 * collection in the Azure Cosmos DB service.
 */
public class UniqueKeyPolicy extends JsonSerializable {
    private List<UniqueKey> uniqueKeys;

    public UniqueKeyPolicy() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the Unique Key policy.
     */
    UniqueKeyPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets or sets collection of {@link UniqueKey} that guarantee uniqueness of documents in collection
     * in the Azure Cosmos DB service.
     *
     * @return the unique keys.
     */
    public Collection<UniqueKey> uniqueKeys() {
        if (this.uniqueKeys == null) {
            this.uniqueKeys = super.getList(Constants.Properties.UNIQUE_KEYS, UniqueKey.class);
            if (this.uniqueKeys == null) {
                this.uniqueKeys = new ArrayList<>();
            }
        }
        return this.uniqueKeys;
    }

    public UniqueKeyPolicy uniqueKeys(List<UniqueKey> uniqueKeys) {
        if (uniqueKeys == null) {
            throw new IllegalArgumentException("uniqueKeys cannot be null.");
        }
        this.uniqueKeys = uniqueKeys;
        return this;
    }

    @Override
    void populatePropertyBag() {
        if (this.uniqueKeys != null) {
            for(UniqueKey uniqueKey: uniqueKeys) {
                uniqueKey.populatePropertyBag();
            }
            super.set(Constants.Properties.UNIQUE_KEYS, uniqueKeys);
        }
    }
}
