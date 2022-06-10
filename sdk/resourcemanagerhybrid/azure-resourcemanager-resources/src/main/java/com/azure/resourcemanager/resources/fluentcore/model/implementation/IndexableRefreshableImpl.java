// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;

/**
 * The implementation for {@link Indexable} and {@link Refreshable}.
 *
 * @param <T> the fluent type of the resource
 */
public abstract class IndexableRefreshableImpl<T>
        extends IndexableImpl
        implements Refreshable<T> {

    protected IndexableRefreshableImpl() {
    }

    protected IndexableRefreshableImpl(String key) {
        super(key);
    }

    @Override
    public abstract T refresh();
}
