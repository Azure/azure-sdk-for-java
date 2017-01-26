/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Completable;

/**
 * Base class for independent child collection class.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 * @param <ParentT> the type of the parent resource
 */
@LangDefinition
public abstract class IndependentChildrenImpl<
        T extends IndependentChild<ManagerT>,
        ImplT extends T,
        InnerT,
        InnerCollectionT,
        ManagerT extends ManagerBase,
        ParentT extends GroupableResource<ManagerT>>
    extends CreatableResourcesImpl<T, ImplT, InnerT>
    implements
        SupportsGettingById<T>,
        SupportsGettingByParent<T, ParentT, ManagerT>,
        SupportsListingByParent<T, ParentT, ManagerT>,
        SupportsDeletingById,
        SupportsDeletingByParent,
        HasManager<ManagerT> {
    protected final InnerCollectionT innerCollection;
    protected final ManagerT manager;

    protected IndependentChildrenImpl(InnerCollectionT innerCollection, ManagerT manager) {
        this.innerCollection = innerCollection;
        this.manager = manager;
    }

    @Override
    public T getByParent(ParentT parentResource, String name) {
        return getByParent(parentResource.resourceGroupName(), parentResource.name(), name);
    }

    @Override
    public T getById(String id) {
        ResourceId resourceId = ResourceId.fromString(id);

        return getByParent(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }

    @Override
    public PagedList<T> listByParent(ParentT parentResource) {
        return listByParent(parentResource.resourceGroupName(), parentResource.name());
    }

    @Override
    public void deleteByParent(String groupName, String parentName, String name) {
        deleteByParentAsync(groupName, parentName, name).await();
    }

    @Override
    public ServiceCall<Void> deleteByParentAsync(String groupName, String parentName, String name, ServiceCallback<Void> callback) {
        return ServiceCall.fromBody(deleteByParentAsync(groupName, parentName, name).<Void>toObservable(), callback);
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        ResourceId resourceId = ResourceId.fromString(id);
        return deleteByParentAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }

    @Override
    public ManagerT manager() {
        return this.manager;
    }
}
