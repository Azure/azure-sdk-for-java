// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.management.Region;
import com.azure.resourcemanager.servicebus.ServiceBusManager;
import com.azure.resourcemanager.servicebus.fluent.NamespacesClient;
import com.azure.resourcemanager.servicebus.fluent.models.SBAuthorizationRuleInner;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRule;
import com.azure.resourcemanager.servicebus.models.NamespaceAuthorizationRules;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import reactor.core.publisher.Mono;

/**
 * Implementation for NamespaceAuthorizationRules.
 */
class NamespaceAuthorizationRulesImpl
    extends ServiceBusChildResourcesImpl<
        NamespaceAuthorizationRule,
        NamespaceAuthorizationRuleImpl,
        SBAuthorizationRuleInner,
        NamespacesClient,
        ServiceBusManager,
        ServiceBusNamespace>
    implements NamespaceAuthorizationRules {
    private final String resourceGroupName;
    private final String namespaceName;
    private final Region region;

    private final ClientLogger logger = new ClientLogger(NamespaceAuthorizationRulesImpl.class);

    NamespaceAuthorizationRulesImpl(String resourceGroupName,
                                    String namespaceName,
                                    Region region,
                                    ServiceBusManager manager) {
        super(manager.serviceClient().getNamespaces(), manager);
        this.resourceGroupName = resourceGroupName;
        this.namespaceName = namespaceName;
        this.region = region;
    }

    @Override
    public NamespaceAuthorizationRuleImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return this.innerModel().deleteAuthorizationRuleAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected Mono<SBAuthorizationRuleInner> getInnerByNameAsync(String name) {
        return this.innerModel().getAuthorizationRuleAsync(this.resourceGroupName, this.namespaceName, name);
    }

    @Override
    protected PagedFlux<SBAuthorizationRuleInner> listInnerAsync() {
        return this.innerModel().listAuthorizationRulesAsync(this.resourceGroupName, this.namespaceName);
    }

    @Override
    protected PagedIterable<SBAuthorizationRuleInner> listInner() {
        return this.innerModel().listAuthorizationRules(this.resourceGroupName,
                this.namespaceName);
    }

    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(String name) {
        return new NamespaceAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                name,
                new SBAuthorizationRuleInner(),
                this.manager());
    }


    @Override
    protected NamespaceAuthorizationRuleImpl wrapModel(SBAuthorizationRuleInner inner) {
        if (inner == null) {
            return null;
        }
        return new NamespaceAuthorizationRuleImpl(this.resourceGroupName,
                this.namespaceName,
                inner.name(),
                inner,
                this.manager());
    }


    @Override
    public PagedIterable<NamespaceAuthorizationRule> listByParent(String resourceGroupName, String parentName) {
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
    public Mono<NamespaceAuthorizationRule> getByParentAsync(String resourceGroup, String parentName, String name) {
        // 'IndependentChildResourcesImpl' will be refactoring to remove all 'ByParent' methods
        // This method is not exposed to end user from any of the derived types of IndependentChildResourcesImpl
        //
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }
}
