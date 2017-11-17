/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRule;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for QueueAuthorizationRules.
 */
@LangDefinition
class QueueAuthorizationRulesImpl
        extends ServiceBusChildResourcesImpl<
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
    private final Region region;

    QueueAuthorizationRulesImpl(String resourceGroupName,
                                String namespaceName,
                                String queueName,
                                Region region,
                                ServiceBusManager manager) {
        super(manager.inner().queues(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.queueName = queueName;
        this.region = region;
    }

    @Override
    public QueueAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name,
                callback);
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerByNameAsync(String name) {
        return this.inner().getAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name);
    }

    @Override
    protected Observable<ServiceResponse<Page<SharedAccessAuthorizationRuleInner>>> listInnerAsync() {
        return this.inner().listAuthorizationRulesWithServiceResponseAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName);
    }

    @Override
    protected PagedList<SharedAccessAuthorizationRuleInner> listInner() {
        return this.inner().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName,
                this.queueName);
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(String name) {
        return new QueueAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name,
                this.region,
                new SharedAccessAuthorizationRuleInner(),
                this.manager());
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new QueueAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }

    @Override
    public PagedList<QueueAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<QueueAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }
}