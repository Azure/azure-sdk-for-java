/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Appliable;

/**
 * The base class for all updatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the implementation type of the fluent model
 */
public abstract class CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        extends CreatableImpl<FluentModelT, InnerModelT, FluentModelImplT> {

    protected CreatableUpdatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    /**
     * Begins an update for a new resource.
     * <p>
     * This is the beginning of the builder pattern used to update top level resources
     * in Azure. The final method completing the definition and starting the actual resource creation
     * process in Azure is {@link Appliable#apply()}.
     *
     * @return the stage of new resource update
     */
    @SuppressWarnings("unchecked")
    public FluentModelImplT update() {
        return (FluentModelImplT) this;
    }
}
