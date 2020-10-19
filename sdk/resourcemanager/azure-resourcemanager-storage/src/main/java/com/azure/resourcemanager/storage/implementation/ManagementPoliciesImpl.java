// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.ManagementPoliciesClient;
import com.azure.resourcemanager.storage.models.ManagementPolicies;
import com.azure.resourcemanager.storage.models.ManagementPolicy;
import com.azure.resourcemanager.storage.fluent.models.ManagementPolicyInner;
import com.azure.resourcemanager.storage.models.ManagementPolicyName;
import reactor.core.publisher.Mono;

public class ManagementPoliciesImpl extends WrapperImpl<ManagementPoliciesClient> implements ManagementPolicies {
    private final StorageManager manager;

    public ManagementPoliciesImpl(StorageManager manager) {
        super(manager.serviceClient().getManagementPolicies());
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
        return this.innerModel().getAsync(resourceGroupName, accountName, ManagementPolicyName.DEFAULT)
            .map(inner -> wrapModel(inner));
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String accountName) {
        return this.innerModel().deleteAsync(resourceGroupName, accountName, ManagementPolicyName.DEFAULT);
    }
}
