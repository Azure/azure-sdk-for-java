/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.storage.BlobContainer;
import com.azure.management.storage.BlobContainers;
import com.azure.management.storage.ImmutabilityPolicy;
import com.azure.management.storage.LegalHold;
import com.azure.management.storage.models.BlobContainerInner;
import com.azure.management.storage.models.BlobContainersInner;
import com.azure.management.storage.models.ImmutabilityPolicyInner;
import com.azure.management.storage.models.LegalHoldInner;
import com.azure.management.storage.models.ListContainerItemInner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

class BlobContainersImpl extends WrapperImpl<BlobContainersInner> implements BlobContainers {
    private final StorageManager manager;

    BlobContainersImpl(StorageManager manager) {
        super(manager.inner().blobContainers());
        this.manager = manager;
    }

    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public BlobContainerImpl defineContainer(String name) {
        return wrapContainerModel(name);
    }

    @Override
    public ImmutabilityPolicyImpl defineImmutabilityPolicy(String name) {
        return wrapImmutabilityPolicyModel(name);
    }

    private BlobContainerImpl wrapContainerModel(String name) {
        return new BlobContainerImpl(name, this.manager());
    }

    private ImmutabilityPolicyImpl wrapImmutabilityPolicyModel(String name) {
        return new ImmutabilityPolicyImpl(name, this.manager());
    }

    private BlobContainerImpl wrapBlobContainerModel(BlobContainerInner inner) {
        return new BlobContainerImpl(inner, manager());
    }

    private ImmutabilityPolicyImpl wrapImmutabilityPolicyModel(ImmutabilityPolicyInner inner) {
        return new ImmutabilityPolicyImpl(inner, manager());
    }

    private Mono<ImmutabilityPolicyInner> getImmutabilityPolicyInnerUsingBlobContainersInnerAsync(String id) {
        String resourceGroupName = IdParsingUtils.getValueFromIdByName(id, "resourceGroups");
        String accountName = IdParsingUtils.getValueFromIdByName(id, "storageAccounts");
        String containerName = IdParsingUtils.getValueFromIdByName(id, "containers");
        BlobContainersInner client = this.inner();
        return client.getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName);
    }

    @Override
    public PagedFlux<ListContainerItemInner> listAsync(String resourceGroupName, String accountName) {
        BlobContainersInner client = this.inner();
        return client.listAsync(resourceGroupName, accountName);
    }

    @Override
    public Mono<BlobContainer> getAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersInner client = this.inner();
        return client.getAsync(resourceGroupName, accountName, containerName)
                .map(inner -> new BlobContainerImpl(inner, manager()));
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersInner client = this.inner();
        return client.deleteAsync(resourceGroupName, accountName, containerName);
    }

    @Override
    public Mono<LegalHold> setLegalHoldAsync(String resourceGroupName, String accountName, String containerName, List<String> tags) {
        BlobContainersInner client = this.inner();
        return client.setLegalHoldAsync(resourceGroupName, accountName, containerName, tags)
                .map(legalHoldInner -> new LegalHoldImpl(legalHoldInner, manager()));
    }

    @Override
    public Mono<LegalHold> clearLegalHoldAsync(String resourceGroupName, String accountName, String containerName, List<String> tags) {
        BlobContainersInner client = this.inner();
        return client.clearLegalHoldAsync(resourceGroupName, accountName, containerName, tags)
                .map(legalHoldInner -> new LegalHoldImpl(legalHoldInner, manager()));
    }

    @Override
    public Mono<ImmutabilityPolicy> getImmutabilityPolicyAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersInner client = this.inner();
        return client.getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName)
                .map(inner -> wrapImmutabilityPolicyModel(inner));
    }

    @Override
    public Mono<ImmutabilityPolicyInner> deleteImmutabilityPolicyAsync(String resourceGroupName, String accountName, String containerName, String ifMatch) {
        return inner().deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, ifMatch);
    }

    @Override
    public Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(String resourceGroupName, String accountName, String containerName, String ifMatch) {
        BlobContainersInner client = this.inner();
        return client.lockImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, ifMatch)
                .map(inner -> new ImmutabilityPolicyImpl(inner, manager()));
    }

    @Override
    public Mono<ImmutabilityPolicy> extendImmutabilityPolicyAsync(String resourceGroupName, String accountName, String containerName, String ifMatch, int immutabilityPeriodSinceCreationInDays, Boolean allowProtectedAppendWrites) {
        BlobContainersInner client = this.inner();
        return client.extendImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, ifMatch, immutabilityPeriodSinceCreationInDays, allowProtectedAppendWrites)
                .map(policyInner -> new ImmutabilityPolicyImpl(policyInner, this.manager));
    }
}