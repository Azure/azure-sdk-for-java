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
    QueuesImpl(QueuesInner innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Namespace parent() {
        return null;
    }

    @Override
    public void deleteByName(String name) {

    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return null;
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return null;
    }

    @Override
    public Observable<Queue> getByNameAsync(String name) {
        return null;
    }

    @Override
    public PagedList<Queue> list() {
        return null;
    }

    @Override
    public Observable<Queue> listAsync() {
        return null;
    }

    @Override
    protected QueueImpl wrapModel(String name) {
        return null;
    }

    @Override
    public Queue getByName(String name) {
        return null;
    }

    @Override
    protected QueueImpl wrapModel(QueueResourceInner inner) {
        return null;
    }

    @Override
    public PagedList<Queue> listByParent(String resourceGroupName, String parentName) {
        return null;
    }

    @Override
    public Queue.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return null;
    }

    @Override
    public Observable<Queue> getByParentAsync(String resourceGroup, String parentName, String name) {
        return null;
    }
}
