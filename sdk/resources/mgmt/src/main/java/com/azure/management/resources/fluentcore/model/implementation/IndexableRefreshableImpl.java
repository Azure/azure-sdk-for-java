/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.model.implementation;

import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.Refreshable;

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
