// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.BlobContainersClient;
import com.azure.resourcemanager.storage.fluent.models.LegalHoldInner;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.BlobContainers;
import com.azure.resourcemanager.storage.models.ImmutabilityPolicy;
import com.azure.resourcemanager.storage.models.LegalHold;
import com.azure.resourcemanager.storage.fluent.models.BlobContainerInner;
import com.azure.resourcemanager.storage.fluent.models.ImmutabilityPolicyInner;
import com.azure.resourcemanager.storage.fluent.models.ListContainerItemInner;
import java.util.List;
import reactor.core.publisher.Mono;

public class BlobContainersImpl extends WrapperImpl<BlobContainersClient> implements BlobContainers {
    private final StorageManager manager;

    public BlobContainersImpl(StorageManager manager) {
        super(manager.serviceClient().getBlobContainers());
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
        return defineImmutabilityPolicy();
    }

    @Override
    public ImmutabilityPolicyImpl defineImmutabilityPolicy() {
        return wrapImmutabilityPolicyModel();
    }

    private BlobContainerImpl wrapContainerModel(String name) {
        return new BlobContainerImpl(name, this.manager());
    }

    private ImmutabilityPolicyImpl wrapImmutabilityPolicyModel() {
        return new ImmutabilityPolicyImpl(this.manager());
    }

    private BlobContainerImpl wrapBlobContainerModel(BlobContainerInner inner) {
        return new BlobContainerImpl(inner, manager());
    }

    private ImmutabilityPolicyImpl wrapImmutabilityPolicyModel(ImmutabilityPolicyInner inner) {
        return new ImmutabilityPolicyImpl(inner, manager());
    }

    @Override
    public PagedFlux<ListContainerItemInner> listAsync(String resourceGroupName, String accountName) {
        BlobContainersClient client = this.innerModel();
        return client.listAsync(resourceGroupName, accountName);
    }

    @Override
    public Mono<BlobContainer> getAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersClient client = this.innerModel();
        return client
            .getAsync(resourceGroupName, accountName, containerName)
            .map(this::wrapBlobContainerModel);
    }

    @Override
    public Mono<Void> deleteAsync(String resourceGroupName, String accountName, String containerName) {
        BlobContainersClient client = this.innerModel();
        return client.deleteAsync(resourceGroupName, accountName, containerName);
    }

    @Override
    public Mono<LegalHold> setLegalHoldAsync(
        String resourceGroupName, String accountName, String containerName, List<String> tags) {
        BlobContainersClient client = this.innerModel();
        return client
            .setLegalHoldAsync(resourceGroupName, accountName, containerName, new LegalHoldInner().withTags(tags))
            .map(legalHoldInner -> new LegalHoldImpl(legalHoldInner, manager()));
    }

    @Override
    public Mono<LegalHold> clearLegalHoldAsync(
        String resourceGroupName, String accountName, String containerName, List<String> tags) {
        BlobContainersClient client = this.innerModel();
        return client
            .clearLegalHoldAsync(resourceGroupName, accountName, containerName, new LegalHoldInner().withTags(tags))
            .map(legalHoldInner -> new LegalHoldImpl(legalHoldInner, manager()));
    }

    @Override
    public Mono<ImmutabilityPolicy> getImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName) {
        return getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, null);
    }

    @Override
    public Mono<ImmutabilityPolicy> getImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue) {
        BlobContainersClient client = this.innerModel();
        return client
            .getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, eTagValue)
            .map(this::wrapImmutabilityPolicyModel);
    }

    @Override
    public Mono<Void> deleteImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName) {
        return deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, null);
    }

    @Override
    public Mono<Void> deleteImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue) {
        return innerModel().deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, eTagValue)
            .then();
    }

    @Override
    public Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName) {
        return lockImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, null);
    }

    @Override
    public Mono<ImmutabilityPolicy> lockImmutabilityPolicyAsync(
        String resourceGroupName, String accountName, String containerName, String eTagValue) {
        BlobContainersClient client = this.innerModel();
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
        BlobContainersClient client = this.innerModel();
        return client
            .extendImmutabilityPolicyAsync(
                resourceGroupName,
                accountName,
                containerName,
                eTagValue,
                new ImmutabilityPolicyInner()
                    .withImmutabilityPeriodSinceCreationInDays(immutabilityPeriodSinceCreationInDays)
                    .withAllowProtectedAppendWrites(allowProtectedAppendWrites))
            .map(policyInner -> new ImmutabilityPolicyImpl(policyInner, this.manager));
    }

    @Override
    public PagedIterable<ListContainerItemInner> list(String resourceGroupName, String accountName) {
        return new PagedIterable<>(this.listAsync(resourceGroupName, accountName));
    }

    @Override
    public BlobContainer get(String resourceGroupName, String accountName, String containerName) {
        return this.getAsync(resourceGroupName, accountName, containerName).block();
    }

    @Override
    public void delete(String resourceGroupName, String accountName, String containerName) {
        this.deleteAsync(resourceGroupName, accountName, containerName).block();
    }

    @Override
    public LegalHold setLegalHold(String resourceGroupName, String accountName, String containerName,
                                  List<String> tags) {
        return this.setLegalHoldAsync(resourceGroupName, accountName, containerName, tags).block();
    }

    @Override
    public LegalHold clearLegalHold(String resourceGroupName, String accountName, String containerName,
                                    List<String> tags) {
        return this.clearLegalHoldAsync(resourceGroupName, accountName, containerName, tags).block();
    }

    @Override
    public ImmutabilityPolicy getImmutabilityPolicy(String resourceGroupName, String accountName,
                                                    String containerName) {
        return this.getImmutabilityPolicyAsync(resourceGroupName, accountName, containerName).block();
    }

    @Override
    public void deleteImmutabilityPolicy(String resourceGroupName, String accountName,
                                                       String containerName) {
        this.deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName).block();
    }

    @Override
    public ImmutabilityPolicy lockImmutabilityPolicy(String resourceGroupName, String accountName,
                                                     String containerName) {
        return this.lockImmutabilityPolicyAsync(resourceGroupName, accountName, containerName).block();
    }

    @Override
    public ImmutabilityPolicy extendImmutabilityPolicy(String resourceGroupName, String accountName,
                                                       String containerName, int immutabilityPeriodSinceCreationInDays,
                                                       Boolean allowProtectedAppendWrites) {
        return this.extendImmutabilityPolicyAsync(resourceGroupName, accountName, containerName,
            immutabilityPeriodSinceCreationInDays, allowProtectedAppendWrites).block();
    }

    @Override
    public void deleteImmutabilityPolicy(String resourceGroupName, String accountName, String containerName,
                                         String eTagValue) {
        this.deleteImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, eTagValue).block();
    }

    @Override
    public ImmutabilityPolicy lockImmutabilityPolicy(String resourceGroupName, String accountName, String containerName,
                                                     String eTagValue) {
        return this.lockImmutabilityPolicyAsync(resourceGroupName, accountName, containerName, eTagValue).block();
    }

    @Override
    public ImmutabilityPolicy extendImmutabilityPolicy(String resourceGroupName, String accountName,
                                                       String containerName, int immutabilityPeriodSinceCreationInDays,
                                                       Boolean allowProtectedAppendWrites, String eTagValue) {
        return this.extendImmutabilityPolicyAsync(resourceGroupName, accountName, containerName,
            immutabilityPeriodSinceCreationInDays, allowProtectedAppendWrites, eTagValue).block();
    }
}
