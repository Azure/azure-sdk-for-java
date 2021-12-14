// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

import java.util.Objects;

/**
 * Represents an index-able model, when used as return type of a method,
 * it indicates invocation of the method may cause side-effect but does
 * not produce a value.
 */
public final class VoidIndexable implements Indexable {
    /**
     * The key.
     */
    private final String key;

    /**
     * Creates VoidIndexable.
     *
     * @param key the key.
     */
    public VoidIndexable(String key) {
        Objects.requireNonNull(key);
        this.key = key;
    }

    @Override
    public String key() {
        return this.key;
    }
}
