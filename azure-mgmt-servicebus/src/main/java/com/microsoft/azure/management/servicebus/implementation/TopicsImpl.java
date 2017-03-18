/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.Topics;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implementation for Topics.
 */
class TopicsImpl
        extends ServiceBusChildResourcesImpl<
        Topic,
        TopicImpl,
        TopicResourceInner,
        TopicsInner,
        ServiceBusManager,
        Namespace>
        implements Topics {
    private final String resourceGroupName;
    private final String namespaceName;

    TopicsImpl(String resourceGroupName, String namespaceName, ServiceBusManager manager) {
        super(manager.inner().topics(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
    }

    @Override
    public Namespace parent() {
        return null;
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
    protected Observable<TopicResourceInner> getInnerByNameAsync(String name) {
        return this.inner().getAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected Observable<ServiceResponse<Page<TopicResourceInner>>> listInnerAsync() {
        return this.inner().listByNamespaceWithServiceResponseAsync(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected PagedList<TopicResourceInner> listInner() {
        return this.inner().listByNamespace(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected TopicImpl wrapModel(String name) {
        return new TopicImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                new TopicResourceInner(),
                this.manager());
    }

    @Override
    protected TopicImpl wrapModel(TopicResourceInner inner) {
        return new TopicImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                inner,
                this.manager());
    }

    @Override
    public PagedList<Topic> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }

    @Override
    public Observable<Topic> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }
}
