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
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation for TopicAuthorizationRule.
 */
@LangDefinition
class TopicAuthorizationRuleImpl extends IndependentChildResourceImpl<TopicAuthorizationRule,
        TopicImpl,
        SharedAccessAuthorizationRuleInner,
        TopicAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        TopicAuthorizationRule,
        TopicAuthorizationRule.Definition,
        TopicAuthorizationRule.Update {
    private final String namespaceName;

    TopicAuthorizationRuleImpl(String resourceGroupName,
                               String namespaceName,
                               String topicName,
                               String name,
                               SharedAccessAuthorizationRuleInner inner,
                               ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.withExistingParentResource(resourceGroupName, topicName);
    }

    @Override
    public Topic parent() {
        return null;
    }

    @Override
    public String namespaceName() {
        return this.namespaceName;
    }

    @Override
    public String topicName() {
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
        return this.manager().inner().topics()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name())
                .map(new Func1<ResourceListKeysInner, AuthorizationKeysImpl>() {
                    @Override
                    public AuthorizationKeysImpl call(ResourceListKeysInner inner) {
                        return new AuthorizationKeysImpl(inner);
                    }
                }).toBlocking().last();
    }

    @Override
    public AuthorizationKeys regenerateKey(Policykey policykey) {
        return this.manager().inner().topics().regenerateKeysAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.topicName(),
                this.name())
            .map(new Func1<ResourceListKeysInner, AuthorizationKeysImpl>() {
                @Override
                public AuthorizationKeysImpl call(ResourceListKeysInner inner) {
                    return new AuthorizationKeysImpl(inner);
                }
            }).toBlocking().last();
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
        return this.manager().inner().topics()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name());
    }

    @Override
    protected Observable<TopicAuthorizationRule> createChildResourceAsync() {
        return null;
    }
}
