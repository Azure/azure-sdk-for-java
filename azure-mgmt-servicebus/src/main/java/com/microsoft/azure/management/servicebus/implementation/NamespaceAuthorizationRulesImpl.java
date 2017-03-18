/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Implementation for NamespaceAuthorizationRules.
 */
class NamespaceAuthorizationRulesImpl
        extends IndependentChildResourcesImpl<
        NamespaceAuthorizationRule,
        NamespaceAuthorizationRuleImpl,
        SharedAccessAuthorizationRuleInner,
        NamespacesInner,
        ServiceBusManager,
        Namespace>
        implements NamespaceAuthorizationRules {
    private final String resourceGroupName;
    private final String namespaceName;

    NamespaceAuthorizationRulesImpl(String resourceGroupName,
                                    String namespaceName,
                                    ServiceBusManager manager) {
        super(manager.inner().namespaces(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
    }

    @Override
    public Namespace parent() {
        return null;
    }

    @Override
    public NamespaceAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<NamespaceAuthorizationRule> getByNameAsync(String name) {
        return this.inner().getAuthorizationRuleAsync(this.resourceGroupName, this.namespaceName, name)
                .map(new Func1<SharedAccessAuthorizationRuleInner, NamespaceAuthorizationRule>() {
                    @Override
                    public NamespaceAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                        return wrapModel(inner);
                    }
                });
    }

    @Override
    public NamespaceAuthorizationRule getByName(String name) {
        return getByNameAsync(name).toBlocking().last();
    }

    @Override
    public Completable deleteByNameAsync(String name) {
        return this.inner().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                name).toCompletable();
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return this.inner().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                name,
                callback);
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).await();
    }

    @Override
    public Observable<NamespaceAuthorizationRule> listAsync() {
        return this.inner().listAuthorizationRulesWithServiceResponseAsync(this.resourceGroupName,
                this.namespaceName).flatMap(new Func1<ServiceResponse<Page<SharedAccessAuthorizationRuleInner>>,
                Observable<NamespaceAuthorizationRule>>() {
            @Override
            public Observable<NamespaceAuthorizationRule> call(ServiceResponse<Page<SharedAccessAuthorizationRuleInner>> r) {
                return Observable.from(r.body().items()).map(new Func1<SharedAccessAuthorizationRuleInner, NamespaceAuthorizationRule>() {
                    @Override
                    public NamespaceAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                        return wrapModel(inner);
                    }
                });
            }
        });
    }

    @Override
    public PagedList<NamespaceAuthorizationRule> list() {
        return this.wrapList(this.inner().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName));
    }

    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(String name) {
        return new NamespaceAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                new SharedAccessAuthorizationRuleInner(),
                this.manager());
    }


    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return new NamespaceAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                inner,
                this.manager());
    }


    @Override
    public PagedList<NamespaceAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<NamespaceAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new NotImplementedException();
    }
}
