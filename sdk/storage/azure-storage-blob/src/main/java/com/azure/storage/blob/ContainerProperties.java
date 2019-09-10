// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.ContainerGetPropertiesHeaders;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.Metadata;
import com.azure.storage.blob.models.PublicAccessType;

import java.time.OffsetDateTime;

public final class ContainerProperties {

    private final Metadata metadata;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final LeaseDurationType leaseDuration;
    private final LeaseStateType leaseState;
    private final LeaseStatusType leaseStatus;
    private final PublicAccessType blobPublicAccess;
    private final boolean hasImmutabilityPolicy;
    private final boolean hasLegalHold;

    ContainerProperties(ContainerGetPropertiesHeaders generatedResponseHeaders) {
        this.metadata = new Metadata(generatedResponseHeaders.metadata());
        this.eTag = generatedResponseHeaders.eTag();
        this.lastModified = generatedResponseHeaders.lastModified();
        this.leaseDuration = generatedResponseHeaders.leaseDuration();
        this.leaseState = generatedResponseHeaders.leaseState();
        this.leaseStatus = generatedResponseHeaders.leaseStatus();
        this.blobPublicAccess = generatedResponseHeaders.blobPublicAccess();
        this.hasImmutabilityPolicy = generatedResponseHeaders.hasImmutabilityPolicy();
        this.hasLegalHold = generatedResponseHeaders.hasLegalHold();
    }

    /**
     * @return the metadata associated with the container
     */
    public Metadata metadata() {
        return metadata;
    }

    /**
     * @return the eTag of the container
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return the time the container was last modified
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return the type of lease on the container
     */
    public LeaseDurationType leaseDuration() {
        return leaseDuration;
    }

    /**
     * @return the lease state of the container
     */
    public LeaseStateType leaseState() {
        return leaseState;
    }

    /**
     * @return the lease status of the container
     */
    public LeaseStatusType leaseStatus() {
        return leaseStatus;
    }

    /**
     * @return the access type for the container
     */
    public PublicAccessType blobPublicAccess() {
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
