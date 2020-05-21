// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.storage.StorageManager;
import com.azure.management.storage.fluent.BlobContainersClient;
import com.azure.management.storage.models.BlobContainer;
import com.azure.management.storage.models.BlobContainers;
import com.azure.management.storage.models.ImmutabilityPolicy;
import com.azure.management.storage.models.LegalHold;
import com.azure.management.storage.fluent.inner.BlobContainerInner;
import com.azure.management.storage.fluent.inner.ImmutabilityPolicyInner;
import com.azure.management.storage.fluent.inner.ListContainerItemInner;
import java.util.List;
import reactor.core.publisher.Mono;

public class BlobContainersImpl extends WrapperImpl<BlobContainersClient> implements BlobContainers {
    private final StorageManager manager;

    public BlobContainersImpl(StorageManager manager) {
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

    public Mono<ImmutabilityPolicyInner> getImmutabilityPolicyInnerUsingBlobContainersClientAsync(String id) {
        String resourceGroupName = IdParsingUtils.getValueFromIdByName(id, "resourceGroups");
        String accountName = IdParsingUtils.getValueFromIdByName(id, "storageAccounts");
        String containerName = IdParsingUtils.getValueFromIdByName(id, "containers");
        BlobContainersClient client = this.inner();
        return client.getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName);
    }

    @Override
    public PagedFlux<ListContainerItemInner> listAsync(String resourceGroupName, String accountName) {
        BlobContainersClient client = this.inner();
        return client.listAsync(resourceGroupName, accountName);
    }

    @Override
    public Mono<BlobContainer> getAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersClient client = this.inner();
        return client
            .getAsync(resourceGroupName, accountName, containerName)
            .map(this::wrapBlobContainerModel);
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersClient client = this.inner();
        return client.deleteAsync(resourceGroupName, accountName, containerName);
    }

    @Override
    public Mono<LegalHold> setLegalHoldAsync(
        String resourceGroupName, String accountName, String containerName, List<String> tags) {
        BlobContainersClient client = this.inner();
        return client
            .setLegalHoldAsync(resourceGroupName, accountName, containerName, tags)
            .map(legalHoldInner -> new LegalHoldImpl(legalHoldInner, manager()));
    }

    @Override
    public Mono<LegalHold> clearLegalHoldAsync(
        String resourceGroupName, String accountName, String containerName, List<String> tags) {
        BlobContainersClient client = this.inner();
        return client
            .clearLegalHoldAsync(resourceGroupName, accountName, containerName, tags)
            .map(legalHoldInner -> new LegalHoldImpl(legalHoldInner, manager()));
    }

    @Override
    public Mono<ImmutabilityPolicy> getImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName) {
        BlobContainersClient client = this.inner();
        return client
            .getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName)
            .map(this::wrapImmutabilityPolicyModel);
    }

    @Override
    public Mono<ImmutabilityPolicyInner> deleteImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName) {
        return deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, null);
    }

    @Override
    public Mono<ImmutabilityPolicyInner> deleteImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue) {
        return inner().deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, eTagValue);
    }

    @Override
    public Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName) {
        return lockImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, null);
    }

    @Override
    public Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue) {
        BlobContainersClient client = this.inner();
        return client
            .lockImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, eTagValue)
            .map(inner -> new ImmutabilityPolicyImpl(inner, manager()));
    }

    @Override
    public Mono<ImmutabilityPolicy> extendImmutabilityPolicyAsync(
        String resourceGroupName,
        String accountName,
        String containerName,
        int immutabilityPeriodSinceCreationInDays,
        Boolean allowProtectedAppendWrites) {
        return extendImmutabilityPolicyAsync(
            resourceGroupName,
            accountName,
            containerName,
            immutabilityPeriodSinceCreationInDays,
            allowProtectedAppendWrites,
            null);
    }

    @Override
    public Mono<ImmutabilityPolicy> extendImmutabilityPolicyAsync(
        String resourceGroupName,
        String accountName,
        String containerName,
        int immutabilityPeriodSinceCreationInDays,
        Boolean allowProtectedAppendWrites,
        String eTagValue) {
        BlobContainersClient client = this.inner();
        return client
            .extendImmutabilityPolicyAsync(
                resourceGroupName,
                accountName,
                containerName,
                eTagValue,
                immutabilityPeriodSinceCreationInDays,
                allowProtectedAppendWrites)
            .map(policyInner -> new ImmutabilityPolicyImpl(policyInner, this.manager));
    }
}
