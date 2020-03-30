/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.storage.ImmutabilityPolicy;
import com.azure.management.storage.ImmutabilityPolicyState;
import com.azure.management.storage.models.BlobContainersInner;
import com.azure.management.storage.models.ImmutabilityPolicyInner;
import reactor.core.publisher.Mono;

class ImmutabilityPolicyImpl
        extends CreatableUpdatableImpl<ImmutabilityPolicy, ImmutabilityPolicyInner, ImmutabilityPolicyImpl>
        implements ImmutabilityPolicy, ImmutabilityPolicy.Definition, ImmutabilityPolicy.Update {
    private final StorageManager manager;
    private String resourceGroupName;
    private String accountName;
    private String containerName;
    private String cifMatch;
    private int cimmutabilityPeriodSinceCreationInDays;
    private String uifMatch;
    private int uimmutabilityPeriodSinceCreationInDays;

    ImmutabilityPolicyImpl(String name, StorageManager manager) {
        super(name, new ImmutabilityPolicyInner());
        this.manager = manager;
        // Set resource name
        this.containerName = name;
        //
    }

    ImmutabilityPolicyImpl(ImmutabilityPolicyInner inner, StorageManager manager) {
        super(inner.getName(), inner);
        this.manager = manager;
        // Set resource name
        this.containerName = inner.getName();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.getId(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.getId(), "storageAccounts");
        this.containerName = IdParsingUtils.getValueFromIdByName(inner.getId(), "containers");
        //
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public Mono<ImmutabilityPolicy> createResourceAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client.createOrUpdateImmutabilityPolicyAsync(this.resourceGroupName, this.accountName, this.containerName, this.cifMatch, this.cimmutabilityPeriodSinceCreationInDays, null)
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<ImmutabilityPolicy> updateResourceAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client.createOrUpdateImmutabilityPolicyAsync(this.resourceGroupName, this.accountName, this.containerName, this.uifMatch, this.uimmutabilityPeriodSinceCreationInDays, null)
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ImmutabilityPolicyInner> getInnerAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client.getImmutabilityPolicyAsync(this.resourceGroupName, this.accountName, this.containerName, this.uifMatch);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }


    @Override
    public String etag() {
        return this.inner().getEtag();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public int immutabilityPeriodSinceCreationInDays() {
        return this.inner().getImmutabilityPeriodSinceCreationInDays();
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public ImmutabilityPolicyState state() {
        return this.inner().getState();
    }

    @Override
    public String type() {
        return this.inner().getType();
    }

    @Override
    public ImmutabilityPolicyImpl withExistingContainer(String resourceGroupName, String accountName, String containerName) {
        this.resourceGroupName = resourceGroupName;
        this.accountName = accountName;
        this.containerName = containerName;
        return this;
    }

    @Override
    public ImmutabilityPolicyImpl withIfMatch(String ifMatch) {
        if (isInCreateMode()) {
            this.cifMatch = ifMatch;
        } else {
            this.uifMatch = ifMatch;
        }
        return this;
    }

    @Override
    public ImmutabilityPolicyImpl withImmutabilityPeriodSinceCreationInDays(int immutabilityPeriodSinceCreationInDays) {
        if (isInCreateMode()) {
            this.cimmutabilityPeriodSinceCreationInDays = immutabilityPeriodSinceCreationInDays;
        } else {
            this.uimmutabilityPeriodSinceCreationInDays = immutabilityPeriodSinceCreationInDays;
        }
        return this;
    }

}