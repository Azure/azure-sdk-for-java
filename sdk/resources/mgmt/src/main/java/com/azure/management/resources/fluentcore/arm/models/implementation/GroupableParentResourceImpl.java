/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.arm.models.implementation;

import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import reactor.core.publisher.Mono;

/**
 * The implementation for GroupableResource.
 * (Internal use only)
 *
 * @param <FluentModelT>     The fluent model type
 * @param <InnerModelT>      Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ManagerT>         the service manager type
 */
public abstract class GroupableParentResourceImpl<
        FluentModelT extends Resource,
        InnerModelT extends com.azure.core.management.Resource,
        FluentModelImplT extends GroupableParentResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT extends ManagerBase>
        extends
        GroupableResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT>
        implements
        GroupableResource<ManagerT, InnerModelT> {

    protected GroupableParentResourceImpl(
            String name,
            InnerModelT innerObject,
            ManagerT manager) {
        super(name, innerObject, manager);
        initializeChildrenFromInner();
    }

    protected abstract Mono<InnerModelT> createInner();

    protected abstract void initializeChildrenFromInner();

    protected void beforeCreating() {}

    protected void afterCreating() {}

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
