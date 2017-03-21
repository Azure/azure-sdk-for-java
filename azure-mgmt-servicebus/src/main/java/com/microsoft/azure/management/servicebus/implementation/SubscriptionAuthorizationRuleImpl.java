/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.Policykey;
import com.microsoft.azure.management.servicebus.Subscription;
import com.microsoft.azure.management.servicebus.SubscriptionAuthorizationRule;
import rx.Observable;

/**
 * Implementation for SubscriptionAuthorizationRule.
 */
@LangDefinition
class SubscriptionAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<SubscriptionAuthorizationRule,
        SubscriptionImpl,
        SharedAccessAuthorizationRuleInner,
        SubscriptionAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        SubscriptionAuthorizationRule,
        SubscriptionAuthorizationRule.Definition,
        SubscriptionAuthorizationRule.Update {
    private final String namespaceName;
    private final String topicName;
    private final Region region;

    SubscriptionAuthorizationRuleImpl(String resourceGroupName,
                                      String namespaceName,
                                      String topicName,
                                      String subscriptionName,
                                      String name,
                                      Region region,
                                      SharedAccessAuthorizationRuleInner inner,
                                      ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.topicName = topicName;
        this.region = region;
        this.withExistingParentResource(resourceGroupName, subscriptionName);
        if (inner.location() == null) {
            inner.withLocation(this.region.toString());
        }
    }

    @Override
    public Subscription parent() {
        return null;
    }

    @Override
    public String namespaceName() {
        return this.namespaceName;
    }

    @Override
    public String topicName() {
        return this.topicName;
    }

    @Override
    public String subscriptionName() {
        return this.parentName;
    }

    @Override
    protected Observable<ResourceListKeysInner> getKeysInnerAsync() {
        return null;
    }

    @Override
    protected Observable<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
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