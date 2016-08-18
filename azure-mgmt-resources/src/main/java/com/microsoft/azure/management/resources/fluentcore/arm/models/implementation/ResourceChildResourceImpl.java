/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;

/**
 * Child resource (can be created/updated on its own) abstract implementation.
 * (Internal use only)
 * @param <FluentModelT> the fluent model type representing the child resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the implementation type of the fluent model
 * @param <ParentImplT> parent implementation
 */
public abstract class ResourceChildResourceImpl<
        FluentModelT,
        InnerModelT extends Resource,
        FluentModelImplT extends ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>, ParentImplT>
    extends ResourceImpl<FluentModelT, InnerModelT, FluentModelImplT>
    implements ChildResource {

    private final ParentImplT parent;

    protected ResourceChildResourceImpl(String name, InnerModelT innerObject, ParentImplT parent) {
        super(name, innerObject);
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
