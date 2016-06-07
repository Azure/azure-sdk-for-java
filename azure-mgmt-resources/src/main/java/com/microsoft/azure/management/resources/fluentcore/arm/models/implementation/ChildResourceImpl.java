/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

/**
 * Child resource abstract implementation.
 * (Internal use only)
 * @param <InnerT> Azure inner child class type
 * @param <ParentImplT> Parent implementation
 */
public abstract class ChildResourceImpl<InnerT, ParentImplT>
    extends IndexableWrapperImpl<InnerT>
    implements ChildResource {

    private final ParentImplT parent;

    protected ChildResourceImpl(String name, InnerT innerObject, ParentImplT parent) {
        super(name, innerObject);
        this.parent = parent;
    }

    protected ParentImplT parent() {
        return this.parent;
    }

    @Override
    public abstract String name();
}
