// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.SubscriptionsClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBSubscriptionInner;
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
        SBSubscriptionInner,
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
    protected Mono<SBSubscriptionInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAsync(this.resourceGroupName, this.namespaceName, this.topicName, name);
    }

    @Override
    protected PagedFlux<SBSubscriptionInner> listInnerAsync() {
        return this.innerModel().listByTopicAsync(this.resourceGroupName, this.namespaceName, this.topicName);
    }

    @Override
    protected PagedIterable<SBSubscriptionInner> listInner() {
        return this.innerModel().listByTopic(this.resourceGroupName, this.namespaceName, this.topicName);
    }

    @Override
    protected ServiceBusSubscriptionImpl wrapModel(String name) {
        return new ServiceBusSubscriptionImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                new SBSubscriptionInner(),
                this.manager());
    }

    @Override
    protected ServiceBusSubscriptionImpl wrapModel(SBSubscriptionInner inner) {
        return new ServiceBusSubscriptionImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                inner.name(),
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
