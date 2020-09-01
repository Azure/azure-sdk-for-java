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
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for NamespaceAuthorizationRules.
 */
@LangDefinition
class NamespaceAuthorizationRulesImpl
        extends ServiceBusChildResourcesImpl<
        NamespaceAuthorizationRule,
        NamespaceAuthorizationRuleImpl,
        SharedAccessAuthorizationRuleInner,
        NamespacesInner,
        ServiceBusManager,
        ServiceBusNamespace>
        implements NamespaceAuthorizationRules {
    private final String resourceGroupName;
    private final String namespaceName;
    private final Region region;

    NamespaceAuthorizationRulesImpl(String resourceGroupName,
                                    String namespaceName,
                                    Region region,
                                    ServiceBusManager manager) {
        super(manager.inner().namespaces(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.region = region;
    }

    @Override
    public NamespaceAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
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
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerByNameAsync(String name) {
        return this.inner().getAuthorizationRuleAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected Observable<ServiceResponse<Page<SharedAccessAuthorizationRuleInner>>> listInnerAsync() {
        return this.inner().listAuthorizationRulesWithServiceResponseAsync(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected PagedList<SharedAccessAuthorizationRuleInner> listInner() {
        return this.inner().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(String name) {
        return new NamespaceAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                this.region,
                new SharedAccessAuthorizationRuleInner(),
                this.manager());
    }


    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new NamespaceAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                this.region,
                inner,
                this.manager());
    }


    @Override
    public PagedList<NamespaceAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<NamespaceAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }
}