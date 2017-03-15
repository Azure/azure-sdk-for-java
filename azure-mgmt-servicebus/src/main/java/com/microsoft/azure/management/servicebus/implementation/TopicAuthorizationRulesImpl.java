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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;

    TopicAuthorizationRulesImpl(String resourceGroupName,
                                String namespaceName,
                                String topicName,
                                ServiceBusManager manager) {
        super(manager.inner().topics(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.topicName = topicName;
    }

    @Override
    public Topic parent() {
        return null;
    }

    @Override
    public TopicAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Observable<TopicAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public TopicAuthorizationRule getByName(String name) {
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
    public Observable<TopicAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    public PagedList<TopicAuthorizationRule> list() {
        return null;
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<TopicAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<TopicAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }
}
