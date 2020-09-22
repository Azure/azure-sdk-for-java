// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.models.UpdatableWithTags;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link GroupableResource} that can update tags as a separate operation.
 *
 * @param <FluentModelT> The fluent model type
 * @param <InnerModelT> Azure inner resource class type
 * @param <FluentModelImplT> the implementation type of the fluent model type
 * @param <ManagerT> the service manager type
 */
public abstract class GroupableParentResourceWithTagsImpl<
        FluentModelT extends Resource,
        InnerModelT extends com.azure.core.management.Resource,
        FluentModelImplT extends
            GroupableParentResourceWithTagsImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT>,
        ManagerT extends Manager<?>>
    extends GroupableParentResourceImpl<FluentModelT, InnerModelT, FluentModelImplT, ManagerT>
    implements UpdatableWithTags<FluentModelT>, AppliableWithTags<FluentModelT> {
    protected GroupableParentResourceWithTagsImpl(String name, InnerModelT innerObject, ManagerT manager) {
        super(name, innerObject, manager);
    }

    @Override
    @SuppressWarnings("unchecked")
    public FluentModelImplT updateTags() {
        return (FluentModelImplT) this;
    }

    @Override
    public FluentModelT applyTags() {
        return applyTagsAsync().block();
    }

    protected abstract Mono<InnerModelT> applyTagsToInnerAsync();

    @Override
    public Mono<FluentModelT> applyTagsAsync() {
        @SuppressWarnings("unchecked")
        final FluentModelT self = (FluentModelT) this;
        return applyTagsToInnerAsync()
            .flatMap(
                inner -> {
                    setInner(inner);
                    return Mono.just(self);
                });
    }
}
