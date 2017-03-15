/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.Queues;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
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
    public Queue.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Observable<Queue> getByNameAsync(String name) {
        return null;
    }

    @Override
    public Queue getByName(String name) {
        return null;
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return null;
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return null;
    }

    @Override
    public void deleteByName(String name) {
    }

    @Override
    public Observable<Queue> listAsync() {
        return null;
    }

    @Override
    public PagedList<Queue> list() {
        return null;
    }


    @Override
    protected QueueImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected QueueImpl wrapModel(QueueResourceInner inner) {
        return null;
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
