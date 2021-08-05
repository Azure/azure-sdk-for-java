// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.TopicsClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBTopicInner;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.Topics;
import reactor.core.publisher.Mono;

/**
 * Implementation for Topics.
 */
class TopicsImpl
    extends ServiceBusChildResourcesImpl<
        Topic,
        TopicImpl,
        SBTopicInner,
        TopicsClient,
        ServiceBusManager,
        ServiceBusNamespace>
    implements Topics {
    private final String resourceGroupName;
    private final String namespaceName;
    private final Region region;

    private final ClientLogger logger = new ClientLogger(TopicsImpl.class);

    TopicsImpl(String resourceGroupName, String namespaceName, Region region, ServiceBusManager manager) {
        super(manager.serviceClient().getTopics(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.region = region;
    }

    @Override
    public TopicImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.innerModel().deleteAsync(this.resourceGroupName,
                this.namespaceName,
                name);
    }

    @Override
    protected Mono<SBTopicInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected PagedFlux<SBTopicInner> listInnerAsync() {
        return this.innerModel().listByNamespaceAsync(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected PagedIterable<SBTopicInner> listInner() {
        return this.innerModel().listByNamespace(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected TopicImpl wrapModel(String name) {
        return new TopicImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                this.region,
                new SBTopicInner(),
                this.manager());
    }

    @Override
    protected TopicImpl wrapModel(SBTopicInner inner) {
        if (inner == null) {
            return null;
        }
        return new TopicImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedIterable<Topic> listByParent(String resourceGroupName, String parentName) {
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
    public Mono<Topic> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }
}
