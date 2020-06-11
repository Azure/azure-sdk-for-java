// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/**
 * Base implementation for Wrapper interface.
 *
 * @param <InnerT> wrapped type
 */
public abstract class WrapperImpl<InnerT> implements HasInner<InnerT> {
    private InnerT innerObject;

    protected WrapperImpl(InnerT innerObject) {
        this.innerObject = innerObject;
    }

    @Override
    public InnerT inner() {
        return this.innerObject;
    }

    /**
     * Sets the inner object of the wrapper.
     * <p>
     * (Internal use only)
     *
     * @param inner an inner object
     */
    public void setInner(InnerT inner) {
        this.innerObject = inner;
    }
}
