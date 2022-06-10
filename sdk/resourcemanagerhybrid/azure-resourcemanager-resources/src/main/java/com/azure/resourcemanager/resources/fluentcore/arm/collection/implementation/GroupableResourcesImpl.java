// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.core.management.Resource;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 *
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 */
public abstract class GroupableResourcesImpl<
        T extends GroupableResource<ManagerT, InnerT>,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT,
        ManagerT extends Manager<?>>
        extends CreatableResourcesImpl<T, ImplT, InnerT>
        implements
        SupportsGettingById<T>,
        SupportsGettingByResourceGroup<T>,
        SupportsDeletingByResourceGroup,
        HasManager<ManagerT> {

    private final InnerCollectionT innerCollection;
    private final ManagerT myManager;

    protected GroupableResourcesImpl(
            InnerCollectionT innerCollection,
            ManagerT manager) {
        this.innerCollection = innerCollection;
        this.myManager = manager;
    }

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
    public Mono<T> getByIdAsync(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        ResourceId resourceId = ResourceId.fromString(id);

        return getByResourceGroupAsync(resourceId.resourceGroupName(), resourceId.name());
    }

    @Override
    public final void deleteByResourceGroup(String groupName, String name) {
        deleteByResourceGroupAsync(groupName, name).block();
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.deleteInnerAsync(resourceGroupName, name)
            .subscribeOn(ResourceManagerUtils.InternalRuntimeContext.getReactorScheduler());
    }

    @Override
    public Mono<Void> deleteByIdAsync(String id) {
        if (CoreUtils.isNullOrEmpty(id)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'id' is required and cannot be null."));
        }
        return deleteByResourceGroupAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public T getByResourceGroup(String resourceGroupName, String name) {
        return getByResourceGroupAsync(resourceGroupName, name).block();
    }

    @Override
    public Mono<T> getByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.getInnerAsync(resourceGroupName, name)
                .map(this::wrapModel);
    }

    protected abstract Mono<InnerT> getInnerAsync(String resourceGroupName, String name);

    protected abstract Mono<Void> deleteInnerAsync(String resourceGroupName, String name);
}
