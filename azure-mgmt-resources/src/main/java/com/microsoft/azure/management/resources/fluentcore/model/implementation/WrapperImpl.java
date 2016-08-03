/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Base implementation for Wrapper interface.
 *
 * @param <InnerT> wrapped type
 */
public abstract class WrapperImpl<InnerT> implements Wrapper<InnerT> {
    private InnerT innerObject;

    protected WrapperImpl(InnerT innerObject) {
        this.innerObject = innerObject;
    }

    @Override
    public InnerT inner() {
        return this.innerObject;
    }

    @Override
    public void setInner(InnerT inner) {
        this.innerObject = inner;
    }
}
