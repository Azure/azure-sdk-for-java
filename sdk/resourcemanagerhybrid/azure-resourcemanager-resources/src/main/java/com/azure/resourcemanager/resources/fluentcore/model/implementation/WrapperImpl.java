// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/**
 * Base implementation for Wrapper interface.
 *
 * @param <InnerT> wrapped type
 */
public abstract class WrapperImpl<InnerT> implements HasInnerModel<InnerT> {
    private InnerT innerObject;

    protected WrapperImpl(InnerT innerObject) {
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
     * @param inner an inner object
     */
    public void setInner(InnerT inner) {
        this.innerObject = inner;
    }
}
