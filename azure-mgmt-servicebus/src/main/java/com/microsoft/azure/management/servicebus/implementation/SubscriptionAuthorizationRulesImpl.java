/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
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
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;
    private final String subscriptionName;
    private final Region region;

    SubscriptionAuthorizationRulesImpl(String resourceGroupName,
                                       String namespaceName,
                                       String topicName,
                                       String subscriptionName,
                                       Region region,
                                       ServiceBusManager manager) {
        super(manager.inner().subscriptions(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
        this.region = region;
    }

    @Override
    public SubscriptionAuthorizationRule.DefinitionStages.Blank define(String name) {
        return null;
    }

    @Override
    public Observable<SubscriptionAuthorizationRule> getByNameAsync(String name) {
        return null;
    }

    @Override
    public SubscriptionAuthorizationRule getByName(String name) {
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
    public Observable<SubscriptionAuthorizationRule> listAsync() {
        return null;
    }

    @Override
    public PagedList<SubscriptionAuthorizationRule> list() {
        return null;
    }

    @Override
    protected SubscriptionAuthorizationRuleImpl wrapModel(String name) {
        return null;
    }


    @Override
    protected SubscriptionAuthorizationRuleImpl wrapModel(SharedAccessAuthorizationRuleInner inner) {
        return null;
    }

    @Override
    public PagedList<SubscriptionAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Observable<SubscriptionAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw new UnsupportedOperationException();
    }
}