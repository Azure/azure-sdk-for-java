// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.implementation.models.PathCreateHeaders;
import com.sun.scenario.effect.Offset;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@code PathItem} contains basic information about a path item that is returned by the service after certain
 * operations.
 */
public class PathItem {
    private String eTag;
    private OffsetDateTime lastModifiedTime;
    private long contentLength;
    private String group;
    private boolean isDirectory;
    private String name;
    private String owner;
    private String permissions;

    public PathItem(PathCreateHeaders generatedHeaders) {
        this.eTag = generatedHeaders.getETag();
        this.lastModifiedTime = generatedHeaders.getLastModified();
    }

    public PathItem(Path path) {
        this.eTag = path.getETag();
        this.lastModifiedTime = OffsetDateTime.parse(path.getLastModified(), DateTimeFormatter.RFC_1123_DATE_TIME);
        this.contentLength = path.getContentLength();
        this.group = path.getGroup();
        if (path.isDirectory() != null) {
            this.isDirectory = path.isDirectory();
        } else {
            this.isDirectory = false;
        }
        this.name = path.getName();
        this.owner = path.getOwner();
        this.permissions = path.getPermissions();
    }

    /**
     * @return the eTag of the path object.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the last modified time of the path object.
     */
    public OffsetDateTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getGroup() {
        return group;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getPermissions() {
        return permissions;
    }
}
