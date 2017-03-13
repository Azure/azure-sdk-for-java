/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRule;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for QueueAuthorizationRules.
 */
class QueueAuthorizationRulesImpl
        extends IndependentChildResourcesImpl<
                QueueAuthorizationRule,
                QueueAuthorizationRuleImpl,
                SharedAccessAuthorizationRuleInner,
                QueuesInner,
                ServiceBusManager,
                Queue>
        implements QueueAuthorizationRules {
    QueueAuthorizationRulesImpl(QueuesInner innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Queue parent() {
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
    public Observable<QueueAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public PagedList<QueueAuthorizationRule> list() {
        return null;
    }

    @Override
    public Observable<QueueAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }

    @Override
    public QueueAuthorizationRule getByName(String name) {
        return null;
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<QueueAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
        return null;
    }

    @Override
    public QueueAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return null;
    }

    @Override
    public Observable<QueueAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        return null;
    }
}
