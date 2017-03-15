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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private final String resourceGroupName;
    private final String namespaceName;
    private final String queueName;

    QueueAuthorizationRulesImpl(String resourceGroupName,
                                String namespaceName,
                                String queueName,
                                ServiceBusManager manager) {
        super(manager.inner().queues(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.queueName = queueName;
    }

    @Override
    public Queue parent() {
        return null;
    }

    @Override
    public QueueAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Observable<QueueAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public QueueAuthorizationRule getByName(String name) {
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
    public Observable<QueueAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    public PagedList<QueueAuthorizationRule> list() {
        return null;
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<QueueAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<QueueAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }
}
