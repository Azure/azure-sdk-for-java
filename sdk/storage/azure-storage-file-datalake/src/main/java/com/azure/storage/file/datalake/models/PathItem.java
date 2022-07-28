// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.util.AccessorUtility;

import java.time.OffsetDateTime;

/**
 * {@code PathItem} contains basic information about a path item that is returned by the service after certain
 * operations.
 */
public class PathItem {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final long contentLength;
    private final String group;
    private final boolean isDirectory;
    private final String name;
    private final String owner;
    private final String permissions;
    private final OffsetDateTime creationTime;
    private final OffsetDateTime expiryTime;
    private String encryptionScope;

    static {
        AccessorUtility.setPathItemAccessor(new AccessorUtility.PathItemAccessor() {
            @Override
            public PathItem setPathItem(PathItem pathItem, String encryptionScope) {
                return pathItem.setEncryptionScope(encryptionScope);
            }
        });
    }

    /**
     * Constructs a {@link PathItem}
     * @param eTag ETag of the path.
     * @param lastModified Datetime when the path was last modified.
     * @param contentLength The content length of the path.
     * @param group The group the path belongs to.
     * @param isDirectory Whether or not the path is a directory.
     * @param name The name of the path.
     * @param owner The owner the path belongs to.
     * @param permissions The permissions set on the path.
     */
    public PathItem(String eTag, OffsetDateTime lastModified, long contentLength, String group, boolean isDirectory,
        String name, String owner, String permissions) {
        this(eTag, lastModified, contentLength, group, isDirectory, name, owner, permissions, null, null);
    }

    /**
     * Constructs a {@link PathItem}
     * @param eTag ETag of the path.
     * @param lastModified Datetime when the path was last modified.
     * @param contentLength The content length of the path.
     * @param group The group the path belongs to.
     * @param isDirectory Whether or not the path is a directory.
     * @param name The name of the path.
     * @param owner The owner the path belongs to.
     * @param permissions The permissions set on the path.
     * @param creationTime The creation time of the path item.
     * @param expiryTime The expiry time of the path item.
     */
    public PathItem(String eTag, OffsetDateTime lastModified, long contentLength, String group, boolean isDirectory,
        String name, String owner, String permissions, OffsetDateTime creationTime, OffsetDateTime expiryTime) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentLength = contentLength;
        this.group = group;
        this.isDirectory = isDirectory;
        this.name = name;
        this.owner = owner;
        this.permissions = permissions;
        this.creationTime = creationTime;
        this.expiryTime = expiryTime;
    }

    /**
     * Get the eTag property: The eTag property.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Get the lastModified property: The lastModified property.
     *
     * @return the lastModified value.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Get the contentLength property: The contentLength property.
     *
     * @return the contentLength value.
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Get the group property: The group property.
     *
     * @return the group value.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Get the isDirectory property: The isDirectory property.
     *
     * @return the isDirectory value.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Get the name property: The name property.
     *
     * @return the name value.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the owner property: The owner property.
     *
     * @return the owner value.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Get the permissions property: The permissions property.
     *
     * @return the permissions value.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Get the creation time property.
     *
     * @return the creation time value.
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Get the expiry time property.
     *
     * @return the expiry time value.
     */
    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    /**
     * Get the encryptionScope property: The name of the encryption scope under which the blob is encrypted.
     *
     * @return the encryptionScope value.
     */
    public String getEncryptionScope() {
        return this.encryptionScope;
    }

    private PathItem setEncryptionScope(String encryptionScope) {
        this.encryptionScope = encryptionScope;
        return this;
    }
}
