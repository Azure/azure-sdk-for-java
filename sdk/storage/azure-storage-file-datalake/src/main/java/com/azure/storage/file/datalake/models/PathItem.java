// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

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
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentLength = contentLength;
        this.group = group;
        this.isDirectory = isDirectory;
        this.name = name;
        this.owner = owner;
        this.permissions = permissions;
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
}
