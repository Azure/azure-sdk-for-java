// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * This class contains the response information returned from the service when getting path properties.
 */
public class PathStatus {
    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final long fileSize;
    private final LeaseStatusType leaseStatus;
    private final LeaseStateType leaseState;
    private final LeaseDurationType leaseDuration;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;
    private final OffsetDateTime expiresOn;
    private final String encryptionScope;
    private final String encryptionContext;
    private final String owner;
    private final String group;
    private final String permissions;
    private final List<PathAccessControlEntry> accessControlList;

    /**
     * Constructs a {@link PathStatus}.
     *
     * @param creationTime Creation time of the file.
     * @param lastModified Datetime when the file was last modified.
     * @param eTag ETag of the file.
     * @param fileSize Size of the file.
     * @param leaseStatus Status of the lease on the file.
     * @param leaseState State of the lease on the file.
     * @param leaseDuration Type of lease on the file.
     * @param isServerEncrypted Flag indicating if the file's content is encrypted on the server.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param encryptionScope The encryption scope of the file.
     * @param encryptionContext The encryption context of the file.
     * @param owner The owner of the file.
     * @param group The group of the file.
     * @param permissions The permissions of the file.
     * @param accessControlList The access control list of the file.
     * pass {@code null}.
     */
    public PathStatus(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long fileSize, final LeaseStatusType leaseStatus, final LeaseStateType leaseState,
        final LeaseDurationType leaseDuration, final Boolean isServerEncrypted, final String encryptionKeySha256,
        final String encryptionScope, final String encryptionContext, final String owner, final String group,
        final String permissions, final String accessControlList) {
        this(creationTime, lastModified, eTag, fileSize, leaseStatus, leaseState, leaseDuration, isServerEncrypted,
            encryptionKeySha256, encryptionScope, encryptionContext, owner, group, permissions, accessControlList,
            null);
    }

    /**
     * Constructs a {@link PathStatus}.
     *
     * @param creationTime Creation time of the file.
     * @param lastModified Datetime when the file was last modified.
     * @param eTag ETag of the file.
     * @param fileSize Size of the file.
     * @param leaseStatus Status of the lease on the file.
     * @param leaseState State of the lease on the file.
     * @param leaseDuration Type of lease on the file.
     * @param isServerEncrypted Flag indicating if the file's content is encrypted on the server.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param encryptionScope The encryption scope of the file.
     * @param encryptionContext The encryption context of the file.
     * @param owner The owner of the file.
     * @param group The group of the file.
     * @param permissions The permissions of the file.
     * @param accessControlList The access control list of the file.
     * pass {@code null}.
     * @param expiresOn the time when the path is going to expire.
     */
    public PathStatus(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long fileSize, final LeaseStatusType leaseStatus, final LeaseStateType leaseState,
        final LeaseDurationType leaseDuration, final Boolean isServerEncrypted, final String encryptionKeySha256,
        final String encryptionScope, final String encryptionContext, final String owner, final String group,
        final String permissions, final String accessControlList, final OffsetDateTime expiresOn) {
        this.creationTime = creationTime;
        this.lastModified = lastModified;
        this.eTag = eTag;
        this.fileSize = fileSize;
        this.leaseStatus = leaseStatus;
        this.leaseState = leaseState;
        this.leaseDuration = leaseDuration;
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.encryptionScope = encryptionScope;
        this.encryptionContext = encryptionContext;
        this.owner = owner;
        this.group = group;
        this.permissions = permissions;
        this.accessControlList = PathAccessControlEntry.parseList(accessControlList);
        this.expiresOn = expiresOn;
    }

    /**
     * Gets the time when the path was created.
     *
     * @return the time when the path was created
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the time when the path was last modified.
     *
     * @return the time when the path was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the eTag of the path.
     *
     * @return the eTag of the path
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the size of the path in bytes.
     *
     * @return the size of the path in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the lease status of the path.
     *
     * @return the lease status of the path
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Gets the lease state of the path.
     *
     * @return the lease state of the path
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * Gets the lease duration if the path is leased.
     *
     * @return the lease duration if the path is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Gets the status of the path being encrypted on the server.
     *
     * @return the status of the path being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the SHA256 of the encryption key used to encrypt the path.
     *
     * @return the key used to encrypt the path
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * Gets the time when the path is going to expire.
     *
     * @return the time when the path is going to expire.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Gets the path's encryption scope.
     *
     * @return the path's encryption scope.
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * Gets the encryption context for this path. Only applicable for files.
     *
     * @return the encryption context for this path. Only applicable for files.
     */
    public String getEncryptionContext() {
        return encryptionContext;
    }

    /**
     * Get the owner property of the path: The owner property.
     *
     * @return the owner value.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Get the group property of the path: The owner property.
     *
     * @return the group value.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Get the permissions property of the path: The permissions property.
     *
     * @return the permissions value.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Optional. The POSIX access control list for the file or directory.
     *
     * @return the access control list.
     */
    public List<PathAccessControlEntry> getAccessControlList() {
        return accessControlList;
    }

}
