/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.servicebus.AccessRights;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.QueueAuthorizationRule;
import rx.Observable;

import java.util.List;

/**
 * Implementation for QueueAuthorizationRule.
 */
@LangDefinition
class QueueAuthorizationRuleImpl extends IndependentChildResourceImpl<QueueAuthorizationRule,
        QueueImpl,
        SharedAccessAuthorizationRuleInner,
        QueueAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        QueueAuthorizationRule,
        QueueAuthorizationRule.Definition,
        QueueAuthorizationRule.Update {
    QueueAuthorizationRuleImpl(String name,
                                         SharedAccessAuthorizationRuleInner inner,
                                         ServiceBusManager manager) {
        super(name, inner, manager);
    }

    @Override
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return null;
    }

    @Override
    protected Observable<QueueAuthorizationRule> createChildResourceAsync() {
        return null;
    }

    @Override
    public Queue parent() {
        return null;
    }

    @Override
    public String namespaceName() {
        return null;
    }

    @Override
    public String queueName() {
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
    public QueueAuthorizationRuleImpl withAccessRight(AccessRights rights) {
        return null;
    }

    @Override
    public QueueAuthorizationRuleImpl withoutAccessRight(AccessRights rights) {
        return null;
    }
}
