/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.servicebus.Policykey;
import com.microsoft.azure.management.servicebus.TopicAuthorizationRule;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for TopicAuthorizationRule.
 */
@LangDefinition
class TopicAuthorizationRuleImpl extends AuthorizationRuleBaseImpl<TopicAuthorizationRule,
        TopicImpl,
        SharedAccessAuthorizationRuleInner,
        TopicAuthorizationRuleImpl,
        ServiceBusManager>
        implements
        TopicAuthorizationRule,
        TopicAuthorizationRule.Definition,
        TopicAuthorizationRule.Update {
    private final String namespaceName;
    private final Region region;

    TopicAuthorizationRuleImpl(String resourceGroupName,
                               String namespaceName,
                               String topicName,
                               String name,
                               Region region,
                               SharedAccessAuthorizationRuleInner inner,
                               ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.region = region;
        this.withExistingParentResource(resourceGroupName, topicName);
        if (inner.location() == null) {
            inner.withLocation(this.region.toString());
        }
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
    protected Observable<SharedAccessAuthorizationRuleInner> getInnerAsync() {
        return this.manager().inner().topics()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name());
    }

    @Override
    protected Observable<TopicAuthorizationRule> createChildResourceAsync() {
        final TopicAuthorizationRule self = this;
        return this.manager().inner().topics().createOrUpdateAuthorizationRuleAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.topicName(),
                this.name(),
                this.inner().rights()).map(new Func1<SharedAccessAuthorizationRuleInner, TopicAuthorizationRule>() {
            @Override
            public TopicAuthorizationRule call(SharedAccessAuthorizationRuleInner inner) {
                setInner(inner);
                return self;
            }
        });
    }

    @Override
    protected Observable<ResourceListKeysInner> getKeysInnerAsync() {
        return this.manager().inner().topics()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name());
    }

    @Override
    protected Observable<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
        return this.manager().inner().topics().regenerateKeysAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.topicName(),
                this.name(),
                policykey);
    }
}