/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

/**
 * Base class for resource collection classes.
 * (Internal use only)
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 * @param <InnerCollectionT> the inner type of the collection object
 * @param <ManagerT> the manager type for this resource provider type
 */
public abstract class GroupableResourcesImpl<
        T extends GroupableResource,
        ImplT extends T,
        InnerT extends Resource,
        InnerCollectionT,
        ManagerT extends ManagerBase>
    extends CreatableResourcesImpl<T, ImplT, InnerT>
    implements
        SupportsGettingById<T>,
        SupportsGettingByGroup<T>,
        SupportsDeletingByGroup,
        HasManager<ManagerT> {

    protected final InnerCollectionT innerCollection;
    protected final ManagerT myManager;
    protected GroupableResourcesImpl(
            InnerCollectionT innerCollection,
            ManagerT manager) {
        this.innerCollection = innerCollection;
        this.myManager = manager;
    }

    @Override
    public ManagerT manager() {
        return this.myManager;
    }

    @Override
    public T getById(String id) {
        return this.getByGroup(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void deleteByGroup(String groupName, String name) {
        deleteByGroupAsync(groupName, name).toBlocking().subscribe();
    }

    @Override
    public ServiceCall<Void> deleteByGroupAsync(String groupName, String name, ServiceCallback<Void> callback) {
        return ServiceCall.create(deleteByGroupAsync(groupName, name).map(new Func1<Void, ServiceResponse<Void>>() {
            @Override
            public ServiceResponse<Void> call(Void aVoid) {
                return new ServiceResponse<>(aVoid, null);
            }
        }), callback);
    }

    @Override
    public Observable<Void> deleteByIdAsync(String id) {
        return deleteByGroupAsync(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }
}
