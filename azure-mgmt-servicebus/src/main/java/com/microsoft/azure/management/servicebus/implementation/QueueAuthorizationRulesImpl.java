/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRule;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;
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
    public QueueAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<QueueAuthorizationRule> getByNameAsync(String name) {
        return this.inner().getAuthorizationRuleAsync(this.resourceGroupName, this.namespaceName, this.queueName, name)
                .map(new Func1<SharedAccessAuthorizationRuleInner, QueueAuthorizationRule>() {
                    @Override
                    public QueueAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public QueueAuthorizationRule getByName(String name) {
        return getByNameAsync(name).toBlocking().last();
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
    public void deleteByName(String name) {
        deleteByNameAsync(name).await();
    }

    @Override
    public Observable<QueueAuthorizationRule> listAsync() {
        return this.inner().listAuthorizationRulesWithServiceResponseAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName).flatMap(new Func1<ServiceResponse<Page<SharedAccessAuthorizationRuleInner>>,
                Observable<QueueAuthorizationRule>>() {
            @Override
            public Observable<QueueAuthorizationRule> call(ServiceResponse<Page<SharedAccessAuthorizationRuleInner>> r) {
                return Observable.from(r.body().items()).map(new Func1<SharedAccessAuthorizationRuleInner, QueueAuthorizationRule>() {
                    @Override
                    public QueueAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                        return wrapModel(inner);
                    }
                });
            }
        });
    }

    @Override
    public PagedList<QueueAuthorizationRule> list() {
        return this.wrapList(this.inner().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName,
                this.queueName));
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(String name) {
        return new QueueAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name,
                new SharedAccessAuthorizationRuleInner(),
                this.manager());
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return new QueueAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                inner.name(),
                inner,
                this.manager());
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
