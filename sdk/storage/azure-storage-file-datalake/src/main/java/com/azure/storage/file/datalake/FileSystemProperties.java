// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.blob.BlobContainerProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.PublicAccessType;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Properties of a file system.
 */
public final class FileSystemProperties {
    private Map<String, String> metadata;
    private String eTag;
    private OffsetDateTime lastModified;
    private LeaseDurationType leaseDuration;
    private LeaseStateType leaseState;
    private LeaseStatusType leaseStatus;
    private PublicAccessType dataLakePublicAccess;
    private boolean hasImmutabilityPolicy;
    private boolean hasLegalHold;

    public FileSystemProperties(BlobContainerProperties blobContainerProperties) {
        this.metadata = blobContainerProperties.getMetadata();
        this.eTag = blobContainerProperties.getETag();
        this.lastModified = blobContainerProperties.getLastModified();
        this.leaseDuration = Transforms.toDataLakeLeaseDurationType(blobContainerProperties.getLeaseDuration());
        this.leaseState = Transforms.toDataLakeLeaseStateType(blobContainerProperties.getLeaseState());
        this.leaseStatus = Transforms.toDataLakeLeaseStatusType(blobContainerProperties.getLeaseStatus());
        this.dataLakePublicAccess = Transforms.toDataLakePublicAccessType(blobContainerProperties
            .getBlobPublicAccess());
        this.hasImmutabilityPolicy = blobContainerProperties.hasImmutabilityPolicy();
        this.hasLegalHold = blobContainerProperties.hasLegalHold();
    }

    /**
     * @return the metadata associated with the file system
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return the eTag of the file system
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the time the file system was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the type of lease on the file system
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the lease state of the file system
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * @return the lease status of the file system
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the access type for the file system
     */
    public PublicAccessType getPublicAccess() {
        return dataLakePublicAccess;
    }

    /**
     * @return the immutability status for the file system
     */
    public boolean hasImmutabilityPolicy() {
        return hasImmutabilityPolicy;
    }

    /**
     * @return the legal hold status for the file system
     */
    public boolean hasLegalHold() {
        return hasLegalHold;
    }
}
