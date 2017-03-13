/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Namespace;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRule;
import com.microsoft.azure.management.servicebus.NamespaceAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

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
    NamespaceAuthorizationRulesImpl(NamespacesInner innerCollection, ServiceBusManager manager) {
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
    public Observable<NamespaceAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public PagedList<NamespaceAuthorizationRule> list() {
        return null;
    }

    @Override
    public Observable<NamespaceAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }

    @Override
    public NamespaceAuthorizationRule getByName(String name) {
        return null;
    }

    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<NamespaceAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
        return null;
    }

    @Override
    public NamespaceAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return null;
    }

    @Override
    public Observable<NamespaceAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        return null;
    }
}
