// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ETagState;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.BlobContainersClient;
import com.azure.resourcemanager.storage.models.ImmutabilityPolicy;
import com.azure.resourcemanager.storage.models.ImmutabilityPolicyState;
import com.azure.resourcemanager.storage.fluent.models.ImmutabilityPolicyInner;
import reactor.core.publisher.Mono;

class ImmutabilityPolicyImpl
    extends CreatableUpdatableImpl<ImmutabilityPolicy, ImmutabilityPolicyInner, ImmutabilityPolicyImpl>
    implements ImmutabilityPolicy, ImmutabilityPolicy.Definition, ImmutabilityPolicy.Update {
    private final StorageManager manager;
    private String resourceGroupName;
    private String accountName;
    private String containerName;
    private int cImmutabilityPeriodSinceCreationInDays;
    private int uImmutabilityPeriodSinceCreationInDays;
    private final ETagState eTagState = new ETagState();

    ImmutabilityPolicyImpl(String name, StorageManager manager) {
        super(name, new ImmutabilityPolicyInner());
        this.manager = manager;
        // Set resource name
        this.containerName = name;
        //
    }

    ImmutabilityPolicyImpl(ImmutabilityPolicyInner inner, StorageManager manager) {
        super(inner.name(), inner);
        this.manager = manager;
        // Set resource name
        this.containerName = inner.name();
        // set resource ancestor and positional variables
        this.resourceGroupName = IdParsingUtils.getValueFromIdByName(inner.id(), "resourceGroups");
        this.accountName = IdParsingUtils.getValueFromIdByName(inner.id(), "storageAccounts");
        this.containerName = IdParsingUtils.getValueFromIdByName(inner.id(), "containers");
        //
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public Mono<ImmutabilityPolicy> createResourceAsync() {
        BlobContainersClient client = this.manager().serviceClient().getBlobContainers();
        return client
            .createOrUpdateImmutabilityPolicyAsync(
                this.resourceGroupName,
                this.accountName,
                this.containerName,
                null,
                new ImmutabilityPolicyInner()
                    .withImmutabilityPeriodSinceCreationInDays(this.cImmutabilityPeriodSinceCreationInDays))
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<ImmutabilityPolicy> updateResourceAsync() {
        BlobContainersClient client = this.manager().serviceClient().getBlobContainers();
        return client
            .createOrUpdateImmutabilityPolicyAsync(
                this.resourceGroupName,
                this.accountName,
                this.containerName,
                this.eTagState.ifMatchValueOnUpdate(this.innerModel().etag()),
                new ImmutabilityPolicyInner()
                    .withImmutabilityPeriodSinceCreationInDays(this.uImmutabilityPeriodSinceCreationInDays))
            .map(innerToFluentMap(this))
            .map(
                self -> {
                    eTagState.clear();
                    return self;
                });
    }

    @Override
    protected Mono<ImmutabilityPolicyInner> getInnerAsync() {
        BlobContainersClient client = this.manager().serviceClient().getBlobContainers();
        return client.getImmutabilityPolicyAsync(this.resourceGroupName, this.accountName, this.containerName, null);
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    public String etag() {
        return this.innerModel().etag();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public int immutabilityPeriodSinceCreationInDays() {
        return this.innerModel().immutabilityPeriodSinceCreationInDays();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public ImmutabilityPolicyState state() {
        return this.innerModel().state();
    }

    @Override
    public String type() {
        return this.innerModel().type();
    }

    @Override
    public ImmutabilityPolicyImpl withExistingContainer(
        String resourceGroupName, String accountName, String containerName) {
        this.resourceGroupName = resourceGroupName;
        this.accountName = accountName;
        this.containerName = containerName;
        return this;
    }

    @Override
    public ImmutabilityPolicyImpl withETagCheck() {
        this.eTagState.withImplicitETagCheckOnCreateOrUpdate(this.isInCreateMode());
        return this;
    }

    @Override
    public ImmutabilityPolicyImpl withETagCheck(String eTagValue) {
        this.eTagState.withExplicitETagCheckOnUpdate(eTagValue);
        return this;
    }

    @Override
    public ImmutabilityPolicyImpl withImmutabilityPeriodSinceCreationInDays(int immutabilityPeriodSinceCreationInDays) {
        if (isInCreateMode()) {
            this.cImmutabilityPeriodSinceCreationInDays = immutabilityPeriodSinceCreationInDays;
        } else {
            this.uImmutabilityPeriodSinceCreationInDays = immutabilityPeriodSinceCreationInDays;
        }
        return this;
    }
}
