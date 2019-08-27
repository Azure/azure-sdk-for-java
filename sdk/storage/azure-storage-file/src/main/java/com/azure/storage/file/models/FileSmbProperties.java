// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;
import java.util.EnumSet;

public class FileSmbProperties {

    private String filePermissionKey;
    private EnumSet<NtfsFileAttributes> ntfsFileAttributes;
    private OffsetDateTime fileCreationTime;
    private OffsetDateTime fileLastWriteTime;
    private OffsetDateTime fileChangeTime;
    private String fileId;
    private String parentId;

    public FileSmbProperties() {
    }

    public FileSmbProperties(FilesCreateResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public FileSmbProperties(DirectorysCreateResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public FileSmbProperties(DirectorysSetPropertiesResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public FileSmbProperties(FilesGetPropertiesResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public FileSmbProperties(DirectorysGetPropertiesResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public FileSmbProperties(FilesDownloadResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public FileSmbProperties(FilesSetHTTPHeadersResponse response) {
        this.filePermissionKey = response.deserializedHeaders().filePermissionKey();
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(response.deserializedHeaders().fileAttributes());
        this.fileCreationTime = response.deserializedHeaders().fileCreationTime();
        this.fileLastWriteTime = response.deserializedHeaders().fileLastWriteTime();
        this.fileChangeTime = response.deserializedHeaders().fileChangeTime();
        this.fileId = response.deserializedHeaders().fileId();
        this.parentId = response.deserializedHeaders().fileParentId();
    }

    public String filePermissionKey() {
        return filePermissionKey;
    }

    public FileSmbProperties filePermissionKey(String filePermissionKey) {
        this.filePermissionKey = filePermissionKey;
        return this;
    }

    public EnumSet<NtfsFileAttributes> ntfsFileAttributes() {
        return ntfsFileAttributes;
    }

    public FileSmbProperties ntfsFileAttributes(EnumSet<NtfsFileAttributes> ntfsFileAttributes) {
        this.ntfsFileAttributes = ntfsFileAttributes;
        return this;
    }

    public OffsetDateTime fileCreationTime() {
        return fileCreationTime;
    }

    public FileSmbProperties fileCreationTime(OffsetDateTime fileCreationTime) {
        this.fileCreationTime = fileCreationTime;
        return this;
    }

    public OffsetDateTime fileLastWriteTime() {
        return fileLastWriteTime;
    }

    public FileSmbProperties fileLastWriteTime(OffsetDateTime fileLastWriteTime) {
        this.fileLastWriteTime = fileLastWriteTime;
        return this;
    }

    public OffsetDateTime fileChangeTime() {
        return fileChangeTime;
    }

    public FileSmbProperties fileChangeTime(OffsetDateTime fileChangeTime) {
        this.fileChangeTime = fileChangeTime;
        return this;
    }

    public String fileId() {
        return fileId;
    }

    public FileSmbProperties fileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public String parentId() {
        return parentId;
    }

    public FileSmbProperties parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }
}
