// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.TopicsClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.TopicAuthorizationRules;
import reactor.core.publisher.Mono;

/**
 * Implementation for TopicAuthorizationRules.
 */
class TopicAuthorizationRulesImpl
    extends ServiceBusChildResourcesImpl<
        TopicAuthorizationRule,
        TopicAuthorizationRuleImpl,
        SBAuthorizationRuleInner,
        TopicsClient,
        ServiceBusManager,
        Topic>
    implements TopicAuthorizationRules {
    private final String resourceGroupName;
    private final String namespaceName;
    private final String topicName;
    private final Region region;

    private final ClientLogger logger = new ClientLogger(TopicAuthorizationRulesImpl.class);

    TopicAuthorizationRulesImpl(String resourceGroupName,
                                String namespaceName,
                                String topicName,
                                Region region,
                                ServiceBusManager manager) {
        super(manager.serviceClient().getTopics(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.topicName = topicName;
        this.region = region;
    }

    @Override
    public TopicAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.innerModel().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name);
    }

    @Override
    protected Mono<SBAuthorizationRuleInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name);
    }

    @Override
    protected PagedFlux<SBAuthorizationRuleInner> listInnerAsync() {
        return this.innerModel().listAuthorizationRulesAsync(
            this.resourceGroupName, this.namespaceName, this.topicName);
    }

    @Override
    protected PagedIterable<SBAuthorizationRuleInner> listInner() {
        return this.innerModel().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName,
                this.topicName);
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(String name) {
        return new TopicAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                name,
                new SBAuthorizationRuleInner(),
                this.manager());
    }

    @Override
    protected TopicAuthorizationRuleImpl wrapModel(SBAuthorizationRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new TopicAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.topicName,
                inner.name(),
                inner,
                this.manager());
    }

    @Override
    public PagedIterable<TopicAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    public Mono<TopicAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }
}
