// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.util.AccessorUtility;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Properties of a file system.
 */
public final class FileSystemProperties {
    private final Map<String, String> metadata;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final LeaseDurationType leaseDuration;
    private final LeaseStateType leaseState;
    private final LeaseStatusType leaseStatus;
    private final PublicAccessType dataLakePublicAccess;
    private final boolean hasImmutabilityPolicy;
    private final boolean hasLegalHold;
    private String encryptionScope;
    private Boolean encryptionScopeOverridePrevented;

    static {
        AccessorUtility
            .setFileSystemPropertiesAccessor((properties, encryptionScope, encryptionScopeOverridePrevented) -> {
                properties.encryptionScope = encryptionScope;
                properties.encryptionScopeOverridePrevented = encryptionScopeOverridePrevented;
                return properties;
            });
    }

    /**
     * Constructs a {@link FileSystemProperties}.
     *
     * @param metadata Metadata associated with the file system.
     * @param eTag ETag of the file system.
     * @param lastModified Datetime when the file system was last modified.
     * @param leaseDuration Type of the lease on the file system.
     * @param leaseState State of the lease on the file system.
     * @param leaseStatus Status of the lease on the file system.
     * @param dataLakePublicAccess Public access status for the file system.
     * @param hasImmutabilityPolicy Flag indicating if the file system has an immutability policy set on it.
     * @param hasLegalHold Flag indicating if the file system has a legal hold.
     */
    public FileSystemProperties(final Map<String, String> metadata, final String eTag,
        final OffsetDateTime lastModified, final LeaseDurationType leaseDuration, final LeaseStateType leaseState,
        final LeaseStatusType leaseStatus, final PublicAccessType dataLakePublicAccess,
        final boolean hasImmutabilityPolicy, final boolean hasLegalHold) {
        this.metadata = metadata;
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.leaseDuration = leaseDuration;
        this.leaseState = leaseState;
        this.leaseStatus = leaseStatus;
        this.dataLakePublicAccess = dataLakePublicAccess;
        this.hasImmutabilityPolicy = hasImmutabilityPolicy;
        this.hasLegalHold = hasLegalHold;
    }

    /**
     * Gets the metadata associated with the file system.
     *
     * @return the metadata associated with the file system
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Gets the eTag of the file system.
     *
     * @return the eTag of the file system
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the time the file system was last modified.
     *
     * @return the time the file system was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the type of lease on the file system.
     *
     * @return the type of lease on the file system
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Gets the lease state of the file system.
     *
     * @return the lease state of the file system
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * Gets the lease status of the file system.
     *
     * @return the lease status of the file system
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Gets the access type for the file system.
     *
     * @return the access type for the file system
     */
    public PublicAccessType getDataLakePublicAccess() {
        return dataLakePublicAccess;
    }

    /**
     * Gets the immutability status for the file system.
     *
     * @return the immutability status for the file system
     */
    public boolean hasImmutabilityPolicy() {
        return hasImmutabilityPolicy;
    }

    /**
     * Gets the legal hold status for the file system.
     *
     * @return the legal hold status for the file system
     */
    public boolean hasLegalHold() {
        return hasLegalHold;
    }

    /**
     * Gets the file system's encryption scope.
     *
     * @return the file system's encryption scope
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * Gets the file system's deny encryption scope override property.
     *
     * @return the file system's deny encryption scope override property.
     */
    public Boolean isEncryptionScopeOverridePrevented() {
        return encryptionScopeOverridePrevented;
    }
}
