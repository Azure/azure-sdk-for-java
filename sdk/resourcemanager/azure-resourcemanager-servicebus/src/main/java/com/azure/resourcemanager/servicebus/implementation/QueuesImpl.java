// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.QueuesClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBQueueInner;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.Queues;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import reactor.core.publisher.Mono;

/**
 * Implementation for Queues.
 */
class QueuesImpl
    extends ServiceBusChildResourcesImpl<
        Queue,
        QueueImpl,
        SBQueueInner,
        QueuesClient,
        ServiceBusManager,
        ServiceBusNamespace>
    implements Queues {
    private final String resourceGroupName;
    private final String namespaceName;
    private final Region region;

    private final ClientLogger logger = new ClientLogger(QueuesImpl.class);

    QueuesImpl(String resourceGroupName, String namespaceName, Region region, ServiceBusManager manager) {
        super(manager.serviceClient().getQueues(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.region = region;
    }

    @Override
    public QueueImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.innerModel().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                name);
    }

    @Override
    protected Mono<SBQueueInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected PagedFlux<SBQueueInner> listInnerAsync() {
        return this.innerModel().listByNamespaceAsync(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected PagedIterable<SBQueueInner> listInner() {
        return this.innerModel().listByNamespace(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected QueueImpl wrapModel(String name) {
        return new QueueImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                this.region,
                new SBQueueInner(),
                this.manager());
    }

    @Override
    protected QueueImpl wrapModel(SBQueueInner inner) {
        if (inner == null) {
            return null;
        }
        return new QueueImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedIterable<Queue> listByParent(String resourceGroupName, String parentName) {
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
    public Mono<Queue> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }
}
