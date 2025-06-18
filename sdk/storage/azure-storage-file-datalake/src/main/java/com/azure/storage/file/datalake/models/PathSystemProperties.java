// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.util.AccessorUtility;

import java.time.OffsetDateTime;

/**
 * This class contains the response information returned from the service when getting system assigned path properties.
 */
public final class PathSystemProperties {
    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final Long fileSize;
    private final Boolean isDirectory;
    private final Boolean isServerEncrypted;
    private final String encryptionKeySha256;
    private final OffsetDateTime expiresOn;
    private final String encryptionScope;
    private final String encryptionContext;
    private final String owner;
    private final String group;
    private final PathPermissions permissions;

    static {
        AccessorUtility.setPathSystemPropertiesAccessor(PathSystemProperties::new);
    }

    /**
     * Default constructor
     */
    public PathSystemProperties() {
        this.creationTime = null;
        this.lastModified = null;
        this.eTag = null;
        this.fileSize = null;
        this.isDirectory = null;
        this.isServerEncrypted = null;
        this.encryptionKeySha256 = null;
        this.expiresOn = null;
        this.encryptionScope = null;
        this.encryptionContext = null;
        this.owner = null;
        this.group = null;
        this.permissions = null;
    }

    /**
     * Constructs a {@link PathSystemProperties}
     * @param creationTime The creation time of the path.
     * @param lastModified The last modified time of the path.
     * @param eTag The eTag of the path.
     * @param fileSize Size of the resource.
     * @param isDirectory A flag indicating if the path is a directory.
     * @param isServerEncrypted A flag indicating if the path's content is encrypted on the server.
     * @param encryptionKeySha256 The SHA256 of the customer provided encryption key used to encrypt the path on the server.
     * @param expiresOn The time when the path is going to expire.
     * @param encryptionScope The encryption scope of the path.
     * @param encryptionContext The additional context for encryption operations on the path.
     * @param owner The owner of the path.
     * @param group The group of the path.
     * @param permissions The {@link PathPermissions}
     */
    PathSystemProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, Long fileSize,
        Boolean isDirectory, Boolean isServerEncrypted, String encryptionKeySha256, OffsetDateTime expiresOn,
        String encryptionScope, String encryptionContext, String owner, String group, PathPermissions permissions) {
        this.creationTime = creationTime;
        this.lastModified = lastModified;
        this.eTag = eTag;
        this.fileSize = fileSize;
        this.isDirectory = isDirectory;
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.expiresOn = expiresOn;
        this.encryptionScope = encryptionScope;
        this.encryptionContext = encryptionContext;
        this.owner = owner;
        this.group = group;
        this.permissions = permissions;
    }

    /**
     * Gets the time when the path was created.
     *
     * @return The creation time of the path.
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the time when the path was last modified.
     *
     * @return The last modified time of the path.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the eTag of the path.
     *
     * @return The eTag of the path.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the size of the path in bytes.
     *
     * @return The size of the path in bytes.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Gets a flag indicating if the path is a directory.
     *
     * @return A flag indicating if the path is a directory.
     */
    public Boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Gets a flag indicating if the path's content is encrypted on the server.
     *
     * @return A flag indicating if the path's content is encrypted on the server.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the SHA256 of the customer provided encryption key used to encrypt the path on the server.
     *
     * @return The SHA256 of the customer provided encryption key used to encrypt the path on the server.
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * Gets the time when the path is going to expire.
     *
     * @return The time when the path is going to expire.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Gets the encryption scope of the path.
     *
     * @return The encryption scope of the path.
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * Gets the additional context for encryption operations on the path.
     *
     * @return The additional context for encryption operations on the path.
     */
    public String getEncryptionContext() {
        return encryptionContext;
    }

    /**
     * Gets the owner of the path.
     *
     * @return The owner of the path.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets the group of the path.
     *
     * @return The group of the path.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the {@link PathPermissions}
     *
     * @return the {@link PathPermissions}
     */
    public PathPermissions getPermissions() {
        return permissions;
    }
}
