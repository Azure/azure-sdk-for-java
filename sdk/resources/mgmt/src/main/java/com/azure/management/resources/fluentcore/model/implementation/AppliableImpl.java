/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.model.implementation;

import com.azure.management.resources.fluentcore.model.Indexable;
import com.azure.management.resources.fluentcore.model.Updatable;
import com.azure.management.resources.fluentcore.model.Appliable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The base class for all updatable resource.
 *
 * @param <FluentModelT>     the fluent model type representing the creatable resource
 * @param <InnerModelT>      the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
public abstract class AppliableImpl<
        FluentModelT extends Indexable,
        InnerModelT,
        FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends
        CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT>
        implements
        Updatable<FluentModelImplT>,
        Appliable<FluentModelT> {
    /**
     * Creates an AppliableImpl.
     *
     * @param name        the appliable name
     * @param innerObject the inner object
     */
    protected AppliableImpl(String name, InnerModelT innerObject) {
        super(name, innerObject);
    }

    @Override
    public final Flux<Indexable> createAsync() {
        throw new IllegalStateException("Internal Error: createAsync cannot be called from UpdatableImpl");
    }

    @Override
    public final Mono<FluentModelT> createResourceAsync() {
        throw new IllegalStateException("Internal Error: createResourceAsync cannot be called from UpdatableImpl");
    }

    @Override
    public abstract Mono<FluentModelT> updateResourceAsync();
}
