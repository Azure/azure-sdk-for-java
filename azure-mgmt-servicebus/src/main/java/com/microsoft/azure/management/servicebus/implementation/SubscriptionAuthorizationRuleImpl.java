/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.*;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
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
    private final String namespaceName;
    private final String topicName;

    SubscriptionAuthorizationRuleImpl(String resourceGroupName,
                                      String namespaceName,
                                      String topicName,
                                      String subscriptionName,
                                      String name,
                                      SharedAccessAuthorizationRuleInner inner,
                                      ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.topicName = topicName;
        this.withExistingParentResource(resourceGroupName, subscriptionName);
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
    public List<AccessRights> rights() {
         if (this.inner().rights() == null) {
             return Collections.unmodifiableList(new ArrayList<AccessRights>());
         }
        return Collections.unmodifiableList(this.inner().rights());
    }

    @Override
    public AuthorizationKeys getKeys() {
        return null;
    }

    @Override
    public AuthorizationKeys regenerateKey(Policykey policykey) {
        return null;
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
