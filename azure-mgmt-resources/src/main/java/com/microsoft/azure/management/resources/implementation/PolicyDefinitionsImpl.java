/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.PolicyDefinition;
import com.microsoft.azure.management.resources.PolicyDefinitions;
import com.microsoft.azure.management.resources.PolicyType;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import rx.Completable;
import rx.Observable;

/**
 * The implementation for {@link ResourceGroups} and its parent interfaces.
 */
final class PolicyDefinitionsImpl
        extends CreatableWrappersImpl<PolicyDefinition, PolicyDefinitionImpl, PolicyDefinitionInner>
        implements PolicyDefinitions {
    private final PolicyDefinitionsInner client;

    /**
     * Creates an instance of the implementation.
     *
     * @param innerClient the inner policies client
     */
    PolicyDefinitionsImpl(final PolicyDefinitionsInner innerClient) {
        this.client = innerClient;
    }

    @Override
    public PagedList<PolicyDefinition> list() {
        return wrapList(client.list());
    }

    @Override
    public PolicyDefinitionImpl getByName(String name) {
        return wrapModel(client.get(name));
    }

    @Override
    public Completable deleteByIdAsync(String name) {
        return client.deleteAsync(name).toCompletable();
    }

    @Override
    public PolicyDefinitionImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected PolicyDefinitionImpl wrapModel(String name) {
        return new PolicyDefinitionImpl(
                new PolicyDefinitionInner().withName(name).withPolicyType(PolicyType.NOT_SPECIFIED).withDisplayName(name),
                client);
    }

    @Override
    protected PolicyDefinitionImpl wrapModel(PolicyDefinitionInner inner) {
        return new PolicyDefinitionImpl(inner, client);
    }

    @Override
    public Observable<PolicyDefinition> listAsync() {
        return wrapPageAsync(client.listAsync());
    }
}
