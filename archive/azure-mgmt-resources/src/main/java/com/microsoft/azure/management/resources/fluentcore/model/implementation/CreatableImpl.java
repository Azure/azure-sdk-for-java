/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import rx.Observable;

/**
 * The base class for all creatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
public abstract class CreatableImpl<
        FluentModelT  extends Indexable,
        InnerModelT,
        FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        Creatable<FluentModelT> {
    /**
     * Creates a CreatableImpl.
     *
     * @param name the creatable name
     * @param innerObject the inner object
     */
    protected CreatableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    @Override
    public final Observable<FluentModelT> applyAsync() {
        throw new IllegalStateException("Internal Error: applyAsync cannot be called from CreatableImpl");
    }

    @Override
    public final Observable<FluentModelT> updateResourceAsync() {
        throw new IllegalStateException("Internal Error: updateResourceAsync cannot be called from CreatableImpl");
    }

    @Override
    public abstract Observable<FluentModelT> createResourceAsync();
}
