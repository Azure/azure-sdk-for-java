/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.Topics;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for Topics.
 */
class TopicsImpl
        extends IndependentChildResourcesImpl<
        Topic,
        TopicImpl,
        TopicResourceInner,
        TopicsInner,
        ServiceBusManager,
        Namespace>
        implements Topics {
    TopicsImpl(TopicsInner innerCollection, ServiceBusManager manager) {
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
    public Observable<Topic> getByNameAsync(String name) {
        return null;
    }

    @Override
    public PagedList<Topic> list() {
        return null;
    }

    @Override
    public Observable<Topic> listAsync() {
        return null;
    }

    @Override
    protected TopicImpl wrapModel(String name) {
        return null;
    }

    @Override
    public Topic getByName(String name) {
        return null;
    }

    @Override
    protected TopicImpl wrapModel(TopicResourceInner inner) {
        return null;
    }

    @Override
    public PagedList<Topic> listByParent(String resourceGroupName, String parentName) {
        return null;
    }

    @Override
    public Topic.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return null;
    }

    @Override
    public Observable<Topic> getByParentAsync(String resourceGroup, String parentName, String name) {
        return null;
    }
}
