// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.SubscriptionsClient;
import com.azure.resourcemanager.servicebus.fluent.models.SubscriptionResourceInner;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscriptions;
import com.azure.resourcemanager.servicebus.models.Topic;
import reactor.core.publisher.Mono;

/**
 * Implementation for Subscriptions.
 */
class ServiceBusSubscriptionsImpl
    extends ServiceBusChildResourcesImpl<
        ServiceBusSubscription,
        ServiceBusSubscriptionImpl,
        SubscriptionResourceInner,
        SubscriptionsClient,
        ServiceBusManager,
        Topic>
    implements ServiceBusSubscriptions {
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;
    private final Region region;

    private final ClientLogger logger = new ClientLogger(ServiceBusSubscriptionsImpl.class);

    protected ServiceBusSubscriptionsImpl(String resourceGroupName,
                                String namespaceName,
                                String topicName,
                                Region region,
                                ServiceBusManager manager) {
        super(manager.serviceClient().getSubscriptions(), manager);
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
    public Mono<Void> deleteByNameAsync(String name) {
        return this.innerModel().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name);
    }

    @Override
    protected Mono<SubscriptionResourceInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAsync(this.resourceGroupName, this.namespaceName, this.topicName, name);
    }

    @Override
    protected PagedFlux<SubscriptionResourceInner> listInnerAsync() {
        return this.innerModel().listAllAsync(this.resourceGroupName, this.namespaceName, this.topicName);
    }

    @Override
    protected PagedIterable<SubscriptionResourceInner> listInner() {
        return this.innerModel().listAll(this.resourceGroupName, this.namespaceName, this.topicName);
    }

    @Override
    protected ServiceBusSubscriptionImpl wrapModel(String name) {
        return new ServiceBusSubscriptionImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                this.region,
                new SubscriptionResourceInner(),
                this.manager());
    }

    @Override
    protected ServiceBusSubscriptionImpl wrapModel(SubscriptionResourceInner inner) {
        return new ServiceBusSubscriptionImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedIterable<ServiceBusSubscription> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    public Mono<ServiceBusSubscription> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }
}
