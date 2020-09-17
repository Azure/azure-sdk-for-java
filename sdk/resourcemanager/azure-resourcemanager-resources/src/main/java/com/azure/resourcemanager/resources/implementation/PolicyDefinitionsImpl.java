// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.PolicyDefinition;
import com.azure.resourcemanager.resources.models.PolicyDefinitions;
import com.azure.resourcemanager.resources.models.PolicyType;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.resourcemanager.resources.fluent.inner.PolicyDefinitionInner;
import com.azure.resourcemanager.resources.fluent.PolicyDefinitionsClient;
import reactor.core.publisher.Mono;

/**
 * The implementation for {@link ResourceGroups} and its parent interfaces.
 */
public final class PolicyDefinitionsImpl
        extends ReadableWrappersImpl<PolicyDefinition, PolicyDefinitionImpl, PolicyDefinitionInner>
        implements PolicyDefinitions {
    private final PolicyDefinitionsClient client;

    /**
     * Creates an instance of the implementation.
     *
     * @param innerClient the inner policies client
     */
    public PolicyDefinitionsImpl(final PolicyDefinitionsClient innerClient) {
        this.client = innerClient;
    }

    @Override
    public PagedIterable<PolicyDefinition> list() {
        return wrapList(client.list());
    }

    @Override
    public PolicyDefinition getByName(String name) {
        return getByNameAsync(name).block();
    }

    @Override
    public Mono<PolicyDefinition> getByNameAsync(String name) {
        return client.getAsync(name)
                .map(this::wrapModel);
    }

    @Override
    public void deleteByName(String name) {
        deleteByNameAsync(name).block();
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return client.deleteAsync(name);
    }

    @Override
    public PolicyDefinitionImpl define(String name) {
        return wrapModel(name);
    }

    protected PolicyDefinitionImpl wrapModel(String name) {
        return new PolicyDefinitionImpl(
                name,
                new PolicyDefinitionInner().withPolicyType(PolicyType.NOT_SPECIFIED).withDisplayName(name),
                client);
    }

    @Override
    protected PolicyDefinitionImpl wrapModel(PolicyDefinitionInner inner) {
        if (inner == null) {
            return null;
        }
        return new PolicyDefinitionImpl(inner.name(), inner, client);
    }

    @Override
    public PagedFlux<PolicyDefinition> listAsync() {
        return wrapPageAsync(client.listAsync());
    }
}
