/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.AccessRights;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRule;
import rx.Observable;

import java.util.List;

/**
 * Implementation for TopicAuthorizationRule.
 */
class TopicAuthorizationRuleImpl extends IndependentChildResourceImpl<TopicAuthorizationRule,
        TopicImpl,
        SharedAccessAuthorizationRuleInner,
        TopicAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        TopicAuthorizationRule,
        TopicAuthorizationRule.Definition,
        TopicAuthorizationRule.Update {
    TopicAuthorizationRuleImpl(String name, SharedAccessAuthorizationRuleInner innerObject, ServiceBusManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public Topic parent() {
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
    public TopicAuthorizationRuleImpl withAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    public TopicAuthorizationRuleImpl withoutAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return null;
    }

    @Override
    protected Observable<TopicAuthorizationRule> createChildResourceAsync() {
        return null;
    }
}
