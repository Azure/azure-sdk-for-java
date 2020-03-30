/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.storage.BlobContainer;
import com.azure.management.storage.ImmutabilityPolicyProperties;
import com.azure.management.storage.LeaseDuration;
import com.azure.management.storage.LeaseState;
import com.azure.management.storage.LeaseStatus;
import com.azure.management.storage.LegalHoldProperties;
import com.azure.management.storage.PublicAccess;
import com.azure.management.storage.models.BlobContainerInner;
import com.azure.management.storage.models.BlobContainersInner;
import reactor.core.publisher.Mono;


import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

class BlobContainerImpl extends CreatableUpdatableImpl<BlobContainer, BlobContainerInner, BlobContainerImpl> implements BlobContainer, BlobContainer.Definition, BlobContainer.Update {
    private final StorageManager manager;
    private String resourceGroupName;
    private String accountName;
    private String containerName;
    private PublicAccess cpublicAccess;
    private Map<String, String> cmetadata;
    private PublicAccess upublicAccess;
    private Map<String, String> umetadata;

    BlobContainerImpl(String name, StorageManager manager) {
        super(name, new BlobContainerInner());
        this.manager = manager;
        // Set resource name
        this.containerName = name;
        //
    }

    BlobContainerImpl(BlobContainerInner inner, StorageManager manager) {
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
    public Mono<BlobContainer> createResourceAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client.createAsync(this.resourceGroupName, this.accountName, this.containerName, cpublicAccess, cmetadata)
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<BlobContainer> updateResourceAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client.updateAsync(this.resourceGroupName, this.accountName, this.containerName, upublicAccess, umetadata)
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<BlobContainerInner> getInnerAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return null; // NOP getInnerAsync implementation as get is not supported
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
    public Boolean hasImmutabilityPolicy() {
        return this.inner().isHasImmutabilityPolicy();
    }

    @Override
    public Boolean hasLegalHold() {
        return this.inner().isHasLegalHold();
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public ImmutabilityPolicyProperties immutabilityPolicy() {
        return this.inner().getImmutabilityPolicy();
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return this.inner().getLastModifiedTime();
    }

    @Override
    public LeaseDuration leaseDuration() {
        return this.inner().getLeaseDuration();
    }

    @Override
    public LeaseState leaseState() {
        return this.inner().getLeaseState();
    }

    @Override
    public LeaseStatus leaseStatus() {
        return this.inner().getLeaseStatus();
    }

    @Override
    public LegalHoldProperties legalHold() {
        return this.inner().getLegalHold();
    }

    @Override
    public Map<String, String> metadata() {
        return this.inner().getMetadata();
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public PublicAccess publicAccess() {
        return this.inner().getPublicAccess();
    }

    @Override
    public String type() {
        return this.inner().getType();
    }

    @Override
    public BlobContainerImpl withExistingBlobService(String resourceGroupName, String accountName) {
        this.resourceGroupName = resourceGroupName;
        this.accountName = accountName;
        return this;
    }

    @Override
    public BlobContainerImpl withPublicAccess(PublicAccess publicAccess) {
        if (isInCreateMode()) {
            this.cpublicAccess = publicAccess;
        } else {
            this.upublicAccess = publicAccess;
        }
        return this;
    }

    @Override
    public BlobContainerImpl withMetadata(Map<String, String> metadata) {
        if (isInCreateMode()) {
            this.cmetadata = metadata;
        } else {
            this.umetadata = metadata;
        }
        return this;
    }

    @Override
    public BlobContainerImpl withMetadata(String name, String value) {
        if (isInCreateMode()) {
            if (this.cmetadata == null) {
                this.cmetadata = new HashMap<>();
            }
            this.cmetadata.put(name, value);
        } else {
            if (this.umetadata == null) {
                this.umetadata = new HashMap<>();
            }
            this.umetadata.put(name, value);
        }
        return this;
    }
}