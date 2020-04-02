// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.storage.ManagementPolicies;
import com.azure.management.storage.ManagementPolicy;
import com.azure.management.storage.models.ManagementPoliciesInner;
import com.azure.management.storage.models.ManagementPolicyInner;
import reactor.core.publisher.Mono;

class ManagementPoliciesImpl extends WrapperImpl<ManagementPoliciesInner> implements ManagementPolicies {
    private final StorageManager manager;

    ManagementPoliciesImpl(StorageManager manager) {
        super(manager.inner().managementPolicies());
        this.manager = manager;
    }

    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public ManagementPolicyImpl define(String name) {
        return wrapModel(name);
    }

    private ManagementPolicyImpl wrapModel(ManagementPolicyInner inner) {
        return new ManagementPolicyImpl(inner, manager());
    }

    private ManagementPolicyImpl wrapModel(String name) {
        return new ManagementPolicyImpl(name, this.manager());
    }

    @Override
    public Mono<ManagementPolicy> getAsync(String resourceGroupName, String accountName) {
        return this.inner().getAsync(resourceGroupName, accountName)
                .map(inner -> wrapModel(inner));
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String accountName) {
        return this.inner().deleteAsync(resourceGroupName, accountName);
    }
}
