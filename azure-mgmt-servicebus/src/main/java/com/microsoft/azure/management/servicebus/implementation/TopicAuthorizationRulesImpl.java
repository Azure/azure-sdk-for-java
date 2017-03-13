/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRule;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for TopicAuthorizationRules.
 */
class TopicAuthorizationRulesImpl
        extends IndependentChildResourcesImpl<
        TopicAuthorizationRule,
        TopicAuthorizationRuleImpl,
        SharedAccessAuthorizationRuleInner,
        TopicsInner,
        ServiceBusManager,
        Topic>
        implements TopicAuthorizationRules {
    TopicAuthorizationRulesImpl(TopicsInner innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Topic parent() {
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
    public Observable<TopicAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public PagedList<TopicAuthorizationRule> list() {
        return null;
    }

    @Override
    public Observable<TopicAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }

    @Override
    public TopicAuthorizationRule getByName(String name) {
        return null;
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<TopicAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
        return null;
    }

    @Override
    public TopicAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return null;
    }

    @Override
    public Observable<TopicAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        return null;
    }
}
