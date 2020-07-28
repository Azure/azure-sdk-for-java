// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

import java.util.UUID;

/**
 * The base implementation for {@link Indexable}.
 */
abstract class IndexableImpl implements Indexable {

    protected String key;

    protected IndexableImpl() {
        this(UUID.randomUUID().toString());
    }

    protected IndexableImpl(String key) {
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


