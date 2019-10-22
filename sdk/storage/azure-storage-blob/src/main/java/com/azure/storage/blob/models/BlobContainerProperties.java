// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import java.time.OffsetDateTime;
import java.util.Map;

public final class BlobContainerProperties {

    private final Map<String, String> metadata;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final LeaseDurationType leaseDuration;
    private final LeaseStateType leaseState;
    private final LeaseStatusType leaseStatus;
    private final PublicAccessType blobPublicAccess;
    private final boolean hasImmutabilityPolicy;
    private final boolean hasLegalHold;

    public BlobContainerProperties(final Map<String, String> metadata, final String eTag,
        final OffsetDateTime lastModified, final LeaseDurationType leaseDuration, final LeaseStateType leaseState,
        final LeaseStatusType leaseStatus, final PublicAccessType blobPublicAccess, final boolean hasImmutabilityPolicy,
        final boolean hasLegalHold) {
        this.metadata = metadata;
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.leaseDuration = leaseDuration;
        this.leaseState = leaseState;
        this.leaseStatus = leaseStatus;
        this.blobPublicAccess = blobPublicAccess;
        this.hasImmutabilityPolicy = hasImmutabilityPolicy;
        this.hasLegalHold = hasLegalHold;
    }

    /**
     * @return the metadata associated with the container
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return the eTag of the container
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the time the container was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the type of lease on the container
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the lease state of the container
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * @return the lease status of the container
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the access type for the container
     */
    public PublicAccessType getBlobPublicAccess() {
        return blobPublicAccess;
    }

    /**
     * @return the immutability status for the container
     */
    public boolean hasImmutabilityPolicy() {
        return hasImmutabilityPolicy;
    }

    /**
     * @return the legal hold status for the container
     */
    public boolean hasLegalHold() {
        return hasLegalHold;
    }




}
