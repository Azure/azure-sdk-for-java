// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.ResourceListKeysInner;
import com.azure.resourcemanager.servicebus.fluent.models.SharedAccessAuthorizationRuleResourceInner;
import com.azure.resourcemanager.servicebus.models.Policykey;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRule;
import reactor.core.publisher.Mono;

/**
 * Implementation for TopicAuthorizationRule.
 */
class TopicAuthorizationRuleImpl
    extends AuthorizationRuleBaseImpl<TopicAuthorizationRule,
        TopicImpl,
        SharedAccessAuthorizationRuleResourceInner,
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
                               SharedAccessAuthorizationRuleResourceInner inner,
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
    protected Mono<SharedAccessAuthorizationRuleResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getTopics()
                .getAuthorizationRuleAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name());
    }

    @Override
    protected Mono<TopicAuthorizationRule> createChildResourceAsync() {
        final TopicAuthorizationRule self = this;
        return this.manager().serviceClient().getTopics().createOrUpdateAuthorizationRuleAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.topicName(),
                this.name(),
                prepareForCreate(this.innerModel()))
            .map(inner -> {
                setInner(inner);
                return self;
            });
    }

    @Override
    protected Mono<ResourceListKeysInner> getKeysInnerAsync() {
        return this.manager().serviceClient().getTopics()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name());
    }

    @Override
    protected Mono<ResourceListKeysInner> regenerateKeysInnerAsync(Policykey policykey) {
        return this.manager().serviceClient().getTopics().regenerateKeysAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.topicName(),
                this.name(),
                policykey);
    }
}
