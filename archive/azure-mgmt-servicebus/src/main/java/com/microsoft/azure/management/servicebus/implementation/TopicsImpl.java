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
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.Topics;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for Topics.
 */
@LangDefinition
class TopicsImpl
        extends ServiceBusChildResourcesImpl<
        Topic,
        TopicImpl,
        TopicInner,
        TopicsInner,
        ServiceBusManager,
        ServiceBusNamespace>
        implements Topics {
    private final String resourceGroupName;
    private final String namespaceName;
    private final Region region;

    TopicsImpl(String resourceGroupName, String namespaceName, Region region, ServiceBusManager manager) {
        super(manager.inner().topics(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.region = region;
    }

    @Override
    public TopicImpl define(String name) {
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
    protected Observable<TopicInner> getInnerByNameAsync(String name) {
        return this.inner().getAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected Observable<ServiceResponse<Page<TopicInner>>> listInnerAsync() {
        return this.inner().listByNamespaceWithServiceResponseAsync(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected PagedList<TopicInner> listInner() {
        return this.inner().listByNamespace(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected TopicImpl wrapModel(String name) {
        return new TopicImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                this.region,
                new TopicInner(),
                this.manager());
    }

    @Override
    protected TopicImpl wrapModel(TopicInner inner) {
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
    public PagedList<Topic> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<Topic> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }
}