// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/**
 * The implementation for {@link Indexable} and {@link HasInnerModel}.
 *
 * @param <InnerT> wrapped type
 */
public abstract class IndexableWrapperImpl<InnerT>
        extends IndexableImpl
        implements HasInnerModel<InnerT> {
    private InnerT innerObject;

    protected IndexableWrapperImpl(InnerT innerObject) {
        super();
        this.innerObject = innerObject;
    }

    @Override
    public InnerT innerModel() {
        return this.innerObject;
    }

    /**
     * Sets the inner object of the wrapper.
     * <p>
     * (Internal use only)
     *
     * @param innerObject an inner object
     */
    public void setInner(InnerT innerObject) {
        this.innerObject = innerObject;
    }
}
