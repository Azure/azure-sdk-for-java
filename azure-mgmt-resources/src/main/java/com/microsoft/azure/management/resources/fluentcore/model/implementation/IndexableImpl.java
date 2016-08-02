/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

import java.util.UUID;

/**
 * The base implementation for {@link Indexable}.
 */
public abstract class IndexableImpl implements Indexable {
    protected String key;

    protected IndexableImpl() {
        this.key = UUID.randomUUID().toString();
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


