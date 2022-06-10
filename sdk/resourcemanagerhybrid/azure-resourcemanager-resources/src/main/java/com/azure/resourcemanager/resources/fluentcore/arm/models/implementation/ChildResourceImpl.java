// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;

/**
 * Child resource abstract implementation.
 * (Internal use only)
 *
 * @param <InnerT> Azure inner child class type
 * @param <ParentImplT> parent implementation
 * @param <ParentT> parent interface
 */
public abstract class ChildResourceImpl<InnerT, ParentImplT extends ParentT, ParentT>
        extends IndexableWrapperImpl<InnerT>
        implements ChildResource<ParentT> {

    private final ParentImplT parent;

    protected ChildResourceImpl(InnerT innerObject, ParentImplT parent) {
        super(innerObject);
        this.parent = parent;
    }

    /**
     * @return parent resource for this child resource
     */
    public ParentImplT parent() {
        return this.parent;
    }

    @Override
    public abstract String name();
}
