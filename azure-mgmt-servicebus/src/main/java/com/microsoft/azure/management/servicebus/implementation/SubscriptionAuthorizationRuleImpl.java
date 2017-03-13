/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.AccessRights;
import com.microsoft.azure.management.servicebus.Subscription;
import com.microsoft.azure.management.servicebus.SubscriptionAuthorizationRule;
import rx.Observable;

import java.util.List;

/**
 * Implementation for SubscriptionAuthorizationRule.
 */
@LangDefinition
class SubscriptionAuthorizationRuleImpl extends IndependentChildResourceImpl<SubscriptionAuthorizationRule,
        SubscriptionImpl,
        SharedAccessAuthorizationRuleInner,
        SubscriptionAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        SubscriptionAuthorizationRule,
        SubscriptionAuthorizationRule.Definition,
        SubscriptionAuthorizationRule.Update {
    SubscriptionAuthorizationRuleImpl(String name, SharedAccessAuthorizationRuleInner innerObject, ServiceBusManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public Subscription parent() {
        return null;
    }

    @Override
    public String namespaceName() {
        return null;
    }

    @Override
    public String topicName() {
        return null;
    }

    @Override
    public String subscriptionName() {
        return null;
    }

    @Override
    public List<AccessRights> rights() {
        return null;
    }

    @Override
    public void listKeys() {

    }

    @Override
    public void regenerateKeys() {

    }

    @Override
    public SubscriptionAuthorizationRuleImpl withAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    public SubscriptionAuthorizationRuleImpl withoutAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return null;
    }

    @Override
    protected Observable<SubscriptionAuthorizationRule> createChildResourceAsync() {
        return null;
    }
}
