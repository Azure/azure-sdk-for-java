/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * The implementation for {@link Indexable} and {@link Wrapper}.
 *
 * @param <InnerT> wrapped type
 */
public abstract class IndexableWrapperImpl<InnerT>
        extends IndexableImpl
        implements Wrapper<InnerT> {
    private InnerT innerObject;
    protected IndexableWrapperImpl(String name, InnerT innerObject) {
        super(name);
        this.innerObject = innerObject;
    }

    @Override
    public InnerT inner() {
        return this.innerObject;
    }
}
