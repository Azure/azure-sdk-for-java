/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Subscription;
import com.microsoft.azure.management.servicebus.Subscriptions;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implementation for Subscriptions.
 */
class SubscriptionsImpl
        extends IndependentChildResourcesImpl<
        Subscription,
        SubscriptionImpl,
        SubscriptionResourceInner,
        SubscriptionsInner,
        ServiceBusManager,
        Topic>
        implements Subscriptions {
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;

    protected SubscriptionsImpl(String resourceGroupName,
                                String namespaceName,
                                String topicName,
                                ServiceBusManager manager) {
        super(manager.inner().subscriptions(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.topicName = topicName;
    }

    @Override
    public Topic parent() {
        return null;
    }

    @Override
    public Subscription.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Observable<Subscription> getByNameAsync(String name) {
        return null;
    }

    @Override
    public Subscription getByName(String name) {
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
    public Observable<Subscription> listAsync() {
        return null;
    }

    @Override
    public PagedList<Subscription> list() {
        return null;
    }

    @Override
    protected SubscriptionImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected SubscriptionImpl wrapModel(SubscriptionResourceInner inner) {
        return null;
    }

    @Override
    public PagedList<Subscription> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<Subscription> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        //
        throw new NotImplementedException();
    }
}
