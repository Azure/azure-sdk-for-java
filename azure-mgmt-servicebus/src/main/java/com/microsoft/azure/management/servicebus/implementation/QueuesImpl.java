/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.Queues;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implementation for Queues.
 */
class QueuesImpl
        extends IndependentChildResourcesImpl<
        Queue,
        QueueImpl,
        QueueResourceInner,
        QueuesInner,
        ServiceBusManager,
        Namespace>
        implements Queues {
    private final String resourceGroupName;
    private final String namespaceName;

    QueuesImpl(String resourceGroupName, String namespaceName, ServiceBusManager manager) {
        super(manager.inner().queues(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
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
    public Observable<Queue> getByNameAsync(String name) {
        return this.inner().getAsync(this.resourceGroupName, this.namespaceName, name)
                .map(new Func1<QueueResourceInner, Queue>() {
                    @Override
                    public Queue call(QueueResourceInner inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public Queue getByName(String name) {
        return getByNameAsync(name).toBlocking().last();
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
    public void deleteByName(String name) {
        deleteByNameAsync(name).await();
    }

    @Override
    public Observable<Queue> listAsync() {
        return this.inner().listByNamspaceWithServiceResponseAsync(this.resourceGroupName, this.namespaceName)
                .flatMap(new Func1<ServiceResponse<Page<QueueResourceInner>>, Observable<Queue>>() {
                    @Override
                    public Observable<Queue> call(ServiceResponse<Page<QueueResourceInner>> r) {
                        return Observable.from(r.body().items()).map(new Func1<QueueResourceInner, Queue>() {
                            @Override
                            public Queue call(QueueResourceInner inner) {
                                return wrapModel(inner);
                            }
                        });
                    }
                });
    }

    @Override
    public PagedList<Queue> list() {
        return this.wrapList(this.inner().listByNamspace(this.resourceGroupName,
                this.namespaceName));
    }


    @Override
    protected QueueImpl wrapModel(String name) {
        return new QueueImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                new QueueResourceInner(),
                this.manager());
    }

    @Override
    protected QueueImpl wrapModel(QueueResourceInner inner) {
        return new QueueImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                inner,
                this.manager());
    }

    @Override
    public PagedList<Queue> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        //
        throw new NotImplementedException();
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        //
        throw new NotImplementedException();
    }

    @Override
    public Observable<Queue> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        //
        throw new NotImplementedException();
    }
}
