/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.servicebus.Subscription;
import com.microsoft.azure.management.servicebus.SubscriptionAuthorizationRule;
import com.microsoft.azure.management.servicebus.SubscriptionAuthorizationRules;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 * Implementation for SubscriptionAuthorizationRules.
 */
class SubscriptionAuthorizationRulesImpl
        extends IndependentChildResourcesImpl<
        SubscriptionAuthorizationRule,
        SubscriptionAuthorizationRuleImpl,
        SharedAccessAuthorizationRuleInner,
        SubscriptionsInner,
        ServiceBusManager,
        Subscription>
        implements SubscriptionAuthorizationRules {
    SubscriptionAuthorizationRulesImpl(SubscriptionsInner innerCollection, ServiceBusManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Subscription parent() {
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
    public Observable<SubscriptionAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public PagedList<SubscriptionAuthorizationRule> list() {
        return null;
    }

    @Override
    public Observable<SubscriptionAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    protected SubscriptionAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }

    @Override
    public SubscriptionAuthorizationRule getByName(String name) {
        return null;
    }

    @Override
    protected SubscriptionAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<SubscriptionAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
        return null;
    }

    @Override
    public SubscriptionAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return null;
    }

    @Override
    public Observable<SubscriptionAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        return null;
    }
}
