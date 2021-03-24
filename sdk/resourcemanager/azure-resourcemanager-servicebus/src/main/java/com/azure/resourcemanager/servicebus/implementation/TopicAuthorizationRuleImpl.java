// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.models.AccessKeysInner;
import com.azure.resourcemanager.servicebus.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.models.RegenerateAccessKeyParameters;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRule;
import reactor.core.publisher.Mono;

/**
 * Implementation for TopicAuthorizationRule.
 */
class TopicAuthorizationRuleImpl
    extends AuthorizationRuleBaseImpl<TopicAuthorizationRule,
        TopicImpl,
        SBAuthorizationRuleInner,
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
                               SBAuthorizationRuleInner inner,
                               ServiceBusManager manager) {
        super(name, inner, manager);
        this.namespaceName = namespaceName;
        this.region = region;
        this.withExistingParentResource(resourceGroupName, topicName);
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
    protected Mono<SBAuthorizationRuleInner> getInnerAsync() {
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
                this.innerModel().rights())
            .map(inner -> {
                setInner(inner);
                return self;
            });
    }

    @Override
    protected Mono<AccessKeysInner> getKeysInnerAsync() {
        return this.manager().serviceClient().getTopics()
                .listKeysAsync(this.resourceGroupName(),
                        this.namespaceName(),
                        this.topicName(),
                        this.name());
    }

    @Override
    protected Mono<AccessKeysInner> regenerateKeysInnerAsync(RegenerateAccessKeyParameters policykey) {
        return this.manager().serviceClient().getTopics().regenerateKeysAsync(this.resourceGroupName(),
                this.namespaceName(),
                this.topicName(),
                this.name(),
                policykey);
    }
}
