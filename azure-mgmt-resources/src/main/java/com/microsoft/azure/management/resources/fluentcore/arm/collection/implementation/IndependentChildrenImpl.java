/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

/**
 * Base class for independent child collection class.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 */
public abstract class IndependentChildrenImpl<
        T extends IndependentChild,
        ImplT extends T,
        InnerT,
        InnerCollectionT,
        ManagerT extends ManagerBase>
    extends CreatableWrappersImpl<T, ImplT, InnerT>
    implements
        SupportsGettingById<T>,
        SupportsGettingByParent<T>,
        SupportsListingByParent<T>,
        SupportsDeleting,
        SupportsDeletingByParent<T> {
    protected final InnerCollectionT innerCollection;
    protected final ManagerT manager;

    protected IndependentChildrenImpl(InnerCollectionT innerCollection, ManagerT manager) {
        this.innerCollection = innerCollection;
        this.manager = manager;
    }

    @Override
    public T getByParent(GroupableResource parentResource, String name) {
        return getByParent(parentResource.resourceGroupName(), parentResource.name(), name);
    }

    @Override
    public T getById(String id) {
        ResourceId resourceId = ResourceId.parseResourceId(id);

        return getByParent(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }

    @Override
    public PagedList<T> listByParent(GroupableResource parentResource) {
        return listByParent(parentResource.resourceGroupName(), parentResource.name());
    }

    @Override
    public void delete(String groupName, String parentName, String name) {
        deleteAsync(groupName, parentName, name).toBlocking().subscribe();
    }

    @Override
    public ServiceCall<Void> deleteAsync(String groupName, String parentName, String name, ServiceCallback<Void> callback) {
        return ServiceCall.create(deleteAsync(groupName, parentName, name).map(new Func1<Void, ServiceResponse<Void>>() {
            @Override
            public ServiceResponse<Void> call(Void aVoid) {
                return new ServiceResponse<>(aVoid, null);
            }
        }), callback);
    }

    @Override
    public Observable<Void> deleteAsync(String id) {
        ResourceId resourceId = ResourceId.parseResourceId(id);
        return deleteAsync(resourceId.resourceGroupName(), resourceId.parent().name(), resourceId.name());
    }
}
