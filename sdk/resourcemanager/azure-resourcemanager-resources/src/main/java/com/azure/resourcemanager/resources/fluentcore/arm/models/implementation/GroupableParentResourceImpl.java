// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import reactor.core.publisher.Mono;

/**
 * The implementation for GroupableResource.
 * (Internal use only)
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ManagerT> the service manager type
 */
public abstract class GroupableParentResourceImpl<
        FluentModelT extends Resource,
        InnerModelT extends com.azure.core.management.Resource,
        FluentModelImplT extends GroupableParentResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT extends Manager<?>>
        extends
        GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT> {

    protected GroupableParentResourceImpl(
            String name,
            InnerModelT innerObject,
            ManagerT manager) {
        super(name, innerObject, manager);
        initializeChildrenFromInner();
    }

    protected abstract Mono<InnerModelT> createInner();

    protected abstract void initializeChildrenFromInner();

    protected void beforeCreating() { }

    protected void afterCreating() { }

    @Override
    public Mono<FluentModelT> createResourceAsync() {
        @SuppressWarnings("unchecked") final FluentModelT self = (FluentModelT) this;
        beforeCreating();
        return createInner()
                .flatMap(inner -> {
                    setInner(inner);
                    try {
                        initializeChildrenFromInner();
                        afterCreating();
                        return Mono.just(self);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }
}
