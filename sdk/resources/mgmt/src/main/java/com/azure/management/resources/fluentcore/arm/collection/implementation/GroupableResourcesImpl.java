/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.resources.fluentcore.arm.collection.implementation;

import com.azure.core.management.Resource;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import reactor.core.publisher.Mono;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 *
 * @param <T>                the individual resource type returned
 * @param <ImplT>            the individual resource implementation
 * @param <InnerT>           the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT>         the manager type for this resource provider type
 */
public abstract class GroupableResourcesImpl<
        T extends GroupableResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT,
        ManagerT extends ManagerBase>
        extends CreatableResourcesImpl<T, ImplT, InnerT>
        implements
        SupportsGettingById<T>,
        SupportsGettingByResourceGroup<T>,
        SupportsDeletingByResourceGroup,
        HasManager<ManagerT>,
        HasInner<InnerCollectionT> {

    private final InnerCollectionT innerCollection;
    private final ManagerT myManager;

    protected GroupableResourcesImpl(
            InnerCollectionT innerCollection,
            ManagerT manager) {
        this.innerCollection = innerCollection;
        this.myManager = manager;
    }

    @Override
    public InnerCollectionT inner() {
        return this.innerCollection;
    }

    @Override
    public ManagerT manager() {
        return this.myManager;
    }

    @Override
    public T getById(String id) {
        return getByIdAsync(id).block();
    }

    @Override
    public final Mono<T> getByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);

        if (resourceId == null) {
            return null;
        }

        return getByResourceGroupAsync(resourceId.resourceGroupName(), resourceId.name());
    }

    @Override
    public final void deleteByResourceGroup(String groupName, String name) {
        deleteByResourceGroupAsync(groupName, name).block();
    }

    @Override
    public Mono<?> deleteByResourceGroupAsync(String groupName, String name) {
        return this.deleteInnerAsync(groupName, name).subscribeOn(SdkContext.getReactorScheduler());
    }

    @Override
    public Mono<?> deleteByIdAsync(String id) {
        return deleteByResourceGroupAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public T getByResourceGroup(String resourceGroupName, String name) {
        return getByResourceGroupAsync(resourceGroupName, name).block();
    }

    @Override
    public Mono<T> getByResourceGroupAsync(String resourceGroupName, String name) {
        return this.getInnerAsync(resourceGroupName, name)
                .map(innerT -> wrapModel(innerT));
    }

    protected abstract Mono<InnerT> getInnerAsync(String resourceGroupName, String name);

    protected abstract Mono<?> deleteInnerAsync(String resourceGroupName, String name);
}
