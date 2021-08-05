// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.QueuesClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.models.Queue;
import com.azure.resourcemanager.servicebus.models.QueueAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.QueueAuthorizationRules;
import reactor.core.publisher.Mono;

/**
 * Implementation for QueueAuthorizationRules.
 */
class QueueAuthorizationRulesImpl
    extends ServiceBusChildResourcesImpl<
        QueueAuthorizationRule,
        QueueAuthorizationRuleImpl,
        SBAuthorizationRuleInner,
        QueuesClient,
        ServiceBusManager,
        Queue>
    implements QueueAuthorizationRules {
    private final String resourceGroupName;
    private final String namespaceName;
    private final String queueName;
    private final Region region;

    private final ClientLogger logger = new ClientLogger(QueueAuthorizationRulesImpl.class);

    QueueAuthorizationRulesImpl(String resourceGroupName,
                                String namespaceName,
                                String queueName,
                                Region region,
                                ServiceBusManager manager) {
        super(manager.serviceClient().getQueues(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.queueName = queueName;
        this.region = region;
    }

    @Override
    public QueueAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.innerModel().deleteAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name);
    }

    @Override
    protected Mono<SBAuthorizationRuleInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAuthorizationRuleAsync(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name);
    }

    @Override
    protected PagedFlux<SBAuthorizationRuleInner> listInnerAsync() {
        return this.innerModel().listAuthorizationRulesAsync(
            this.resourceGroupName, this.namespaceName, this.queueName);
    }

    @Override
    protected PagedIterable<SBAuthorizationRuleInner> listInner() {
        return this.innerModel().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName,
                this.queueName);
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(String name) {
        return new QueueAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                name,
                new SBAuthorizationRuleInner(),
                this.manager());
    }

    @Override
    protected QueueAuthorizationRuleImpl wrapModel(SBAuthorizationRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new QueueAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                this.queueName,
                inner.name(),
                inner,
                this.manager());
    }

    @Override
    public PagedIterable<QueueAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Mono<QueueAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }
}
