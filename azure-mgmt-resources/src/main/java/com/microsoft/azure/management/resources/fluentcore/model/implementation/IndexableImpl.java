/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * The base implementation for {@link Indexable}.
 */
public abstract class IndexableImpl implements Indexable {
    protected String key;

    protected IndexableImpl(String key) {
        this.key = key;
    }

    /**
     * Set the indexing key.
     *
     * @param key the indexing key
     */
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public String toString() {
        return this.key();
    }
}


