// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import reactor.core.publisher.Mono;

class BlobContainerImpl extends CreatableUpdatableImpl<BlobContainer, BlobContainerInner, BlobContainerImpl>
    implements BlobContainer, BlobContainer.Definition, BlobContainer.Update {
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
    public Mono<BlobContainer> createResourceAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client
            .createAsync(this.resourceGroupName, this.accountName, this.containerName,
                this.inner().withPublicAccess(cpublicAccess).withMetadata(cmetadata))
            .map(innerToFluentMap(this));
    }

    @Override
    public Mono<BlobContainer> updateResourceAsync() {
        BlobContainersInner client = this.manager().inner().blobContainers();
        return client
            .updateAsync(this.resourceGroupName, this.accountName, this.containerName,
                this.inner().withPublicAccess(upublicAccess).withMetadata(umetadata))
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<BlobContainerInner> getInnerAsync() {
        return this.manager().inner().blobContainers().getAsync(resourceGroupName, accountName, containerName);
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public String etag() {
        return this.inner().etag();
    }

    @Override
    public Boolean hasImmutabilityPolicy() {
        return this.inner().hasImmutabilityPolicy();
    }

    @Override
    public Boolean hasLegalHold() {
        return this.inner().hasLegalHold();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public ImmutabilityPolicyProperties immutabilityPolicy() {
        return this.inner().immutabilityPolicy();
    }

    @Override
    public OffsetDateTime lastModifiedTime() {
        return this.inner().lastModifiedTime();
    }

    @Override
    public LeaseDuration leaseDuration() {
        return this.inner().leaseDuration();
    }

    @Override
    public LeaseState leaseState() {
        return this.inner().leaseState();
    }

    @Override
    public LeaseStatus leaseStatus() {
        return this.inner().leaseStatus();
    }

    @Override
    public LegalHoldProperties legalHold() {
        return this.inner().legalHold();
    }

    @Override
    public Map<String, String> metadata() {
        return this.inner().metadata();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public PublicAccess publicAccess() {
        return this.inner().publicAccess();
    }

    @Override
    public String type() {
        return this.inner().type();
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
