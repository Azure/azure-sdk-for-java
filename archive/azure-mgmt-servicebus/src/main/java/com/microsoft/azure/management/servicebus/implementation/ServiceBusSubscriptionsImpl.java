/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.management.servicebus.ServiceBusSubscriptions;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for Subscriptions.
 */
@LangDefinition
class ServiceBusSubscriptionsImpl
        extends ServiceBusChildResourcesImpl<
        ServiceBusSubscription,
        ServiceBusSubscriptionImpl,
        SubscriptionInner,
        SubscriptionsInner,
        ServiceBusManager,
        Topic>
        implements ServiceBusSubscriptions {
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;
    private final Region region;

    protected ServiceBusSubscriptionsImpl(String resourceGroupName,
                                String namespaceName,
                                String topicName,
                                Region region,
                                ServiceBusManager manager) {
        super(manager.inner().subscriptions(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.topicName = topicName;
        this.region = region;
    }

    @Override
    public ServiceBusSubscriptionImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                callback);
    }

    @Override
    protected Observable<SubscriptionInner> getInnerByNameAsync(String name) {
        return this.inner().getAsync(this.resourceGroupName, this.namespaceName, this.topicName, name);
    }

    @Override
    protected Observable<ServiceResponse<Page<SubscriptionInner>>> listInnerAsync() {
        return this.inner().listByTopicWithServiceResponseAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName);
    }

    @Override
    protected PagedList<SubscriptionInner> listInner() {
        return this.inner().listByTopic(this.resourceGroupName,
                this.namespaceName,
                this.topicName);
    }

    @Override
    protected ServiceBusSubscriptionImpl wrapModel(String name) {
        return new ServiceBusSubscriptionImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                this.region,
                new SubscriptionInner(),
                this.manager());
    }

    @Override
    protected ServiceBusSubscriptionImpl wrapModel(SubscriptionInner inner) {
        return new ServiceBusSubscriptionImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedList<ServiceBusSubscription> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<ServiceBusSubscription> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }
}