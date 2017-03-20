/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.Queues;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implementation for Queues.
 */
class QueuesImpl
        extends ServiceBusChildResourcesImpl<
        Queue,
        QueueImpl,
        QueueResourceInner,
        QueuesInner,
        ServiceBusManager,
        Namespace>
        implements Queues {
    private final String resourceGroupName;
    private final String namespaceName;
    private final Region region;

    QueuesImpl(String resourceGroupName, String namespaceName, Region region, ServiceBusManager manager) {
        super(manager.inner().queues(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.region = region;
    }

    @Override
    public Namespace parent() {
        // TODO: Remove the parent getter
        return null;
    }

    @Override
    public QueueImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                name,
                callback);
    }

    @Override
    protected Observable<QueueResourceInner> getInnerByNameAsync(String name) {
        return this.inner().getAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected Observable<ServiceResponse<Page<QueueResourceInner>>> listInnerAsync() {
        return this.inner().listByNamspaceWithServiceResponseAsync(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected PagedList<QueueResourceInner> listInner() {
        return this.inner().listByNamspace(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected QueueImpl wrapModel(String name) {
        return new QueueImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                this.region,
                new QueueResourceInner(),
                this.manager());
    }

    @Override
    protected QueueImpl wrapModel(QueueResourceInner inner) {
        return new QueueImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedList<Queue> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }

    @Override
    public Observable<Queue> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }
}
