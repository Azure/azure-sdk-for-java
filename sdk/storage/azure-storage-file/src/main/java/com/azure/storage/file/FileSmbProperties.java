// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.DateTimeRfc1123;
import com.azure.storage.file.models.DirectorysCreateResponse;
import com.azure.storage.file.models.DirectorysGetPropertiesResponse;
import com.azure.storage.file.models.DirectorysSetPropertiesResponse;
import com.azure.storage.file.models.FilesCreateResponse;
import com.azure.storage.file.models.FilesDownloadResponse;
import com.azure.storage.file.models.FilesGetPropertiesResponse;
import com.azure.storage.file.models.FilesSetHTTPHeadersResponse;
import com.azure.storage.file.models.NtfsFileAttributes;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

public class FileSmbProperties {

    private String filePermissionKey;
    private EnumSet<NtfsFileAttributes> ntfsFileAttributes;
    private OffsetDateTime fileCreationTime;
    private OffsetDateTime fileLastWriteTime;
    private final OffsetDateTime fileChangeTime;
    private final String fileId;
    private final String parentId;

    /**
     * Default constructor
     */
    public FileSmbProperties() {
        // Non user-settable properties
        fileChangeTime = null;
        fileId = null;
        parentId = null;
    }

    /**
     * @return The file's permission key.
     */
    public String filePermissionKey() {
        return filePermissionKey;
    }

    /**
     * @return The file's {@link NtfsFileAttributes}.
     */
    public EnumSet<NtfsFileAttributes> ntfsFileAttributes() {
        return ntfsFileAttributes;
    }

    /**
     * @return The file's creation time.
     */
    public OffsetDateTime fileCreationTime() {
        return fileCreationTime;
    }

    /**
     * @return The file's last write time.
     */
    public OffsetDateTime fileLastWriteTime() {
        return fileLastWriteTime;
    }

    /**
     * @return The file's change time.
     */
    public OffsetDateTime fileChangeTime() {
        return fileChangeTime;
    }

    /**
     * @return The file's ID.
     */
    public String fileId() {
        return fileId;
    }

    /**
     * @return The file's parent ID.
     */
    public String parentId() {
        return parentId;
    }

    /**
     * Sets the file permission key.
     * @param filePermissionKey The file permission key.
     * @return the updated FileSmbProperties object.
     */
    public FileSmbProperties filePermissionKey(String filePermissionKey) {
        this.filePermissionKey = filePermissionKey;
        return this;
    }

    /**
     * Sets the ntfs file attributes.
     * @param ntfsFileAttributes An enum set of the ntfs file attributes.
     * @return the updated FileSmbProperties object.
     */
    public FileSmbProperties ntfsFileAttributes(EnumSet<NtfsFileAttributes> ntfsFileAttributes) {
        this.ntfsFileAttributes = ntfsFileAttributes;
        return this;
    }

    /**
     * Sets the file creation time.
     * @param fileCreationTime The file creation time.
     * @return the updated FileSmbProperties object..
     */
    public FileSmbProperties fileCreationTime(OffsetDateTime fileCreationTime) {
        this.fileCreationTime = fileCreationTime;
        return this;
    }

    /**
     * Sets the file last write time.
     * @param fileLastWriteTime The file last write time.
     * @return the updated FileSmbProperties object.
     */
    public FileSmbProperties fileLastWriteTime(OffsetDateTime fileLastWriteTime) {
        this.fileLastWriteTime = fileLastWriteTime;
        return this;
    }

    // HELPER METHODS

    /**
     * Determines the value of the file permission header.
     * @param filePermission The file permission.
     * @param defaultValue The default file permission header value.
     * @return The value of the file permission header
     */
    String filePermission(String filePermission, String defaultValue) {
        return (filePermission == null) && (filePermissionKey == null)
            ? defaultValue
            : filePermission;
    }

    /**
     * Determines the value of the ntfs attributes header.
     * @param defaultValue The default ntfs attributes header value.
     * @return The value of the ntfs attributes header
     */
    String ntfsFileAttributes(String defaultValue) {
        return ntfsFileAttributes == null
            ? defaultValue
            : NtfsFileAttributes.toString(ntfsFileAttributes);
    }

    /**
     * Determines the value of the creation time header.
     * @param defaultValue The default creation time header value.
     * @return The value of the creation time header
     */
    String fileCreationTime(String defaultValue) {
        return fileCreationTime == null
            ? defaultValue
            : parseFileSMBDate(fileCreationTime);
    }

    /**
     * Determines the value of the last write time header.
     * @param defaultValue The default last write time header value.
     * @return The value of the last write time header
     */
    String fileLastWriteTime(String defaultValue) {
        return fileLastWriteTime == null
            ? defaultValue
            : parseFileSMBDate(fileLastWriteTime);
    }

    /**
     * Given an <code>OffsetDateTime</code>, generates a {@code String} representing a date in the format needed for
     * file SMB properties
     * @param time the <code>OffsetDateTime</code> to be interpreted as a {@code String}
     * @return The {@code String} representing the date
     */
    private static String parseFileSMBDate(OffsetDateTime time) {
        return time.format(DateTimeFormatter.ofPattern(FileConstants.SMB_DATE_STRING));
    }

    /**
     * Creates a new FileSmbProperties object from HttpHeaders
     * @param httpHeaders The headers to construct FileSmbProperties from
     */
    FileSmbProperties(HttpHeaders httpHeaders) {
        this.filePermissionKey = httpHeaders.value(FileConstants.HeaderConstants.FILE_PERMISSION_KEY);
        this.ntfsFileAttributes = NtfsFileAttributes.toAttributes(httpHeaders.value(FileConstants.HeaderConstants.FILE_ATTRIBUTES));
        this.fileCreationTime = OffsetDateTime.parse(httpHeaders.value(FileConstants.HeaderConstants.FILE_CREATION_TIME));
        this.fileLastWriteTime = OffsetDateTime.parse(httpHeaders.value(FileConstants.HeaderConstants.FILE_LAST_WRITE_TIME));
        this.fileChangeTime = OffsetDateTime.parse(httpHeaders.value(FileConstants.HeaderConstants.FILE_CHANGE_TIME));
        this.fileId = httpHeaders.value(FileConstants.HeaderConstants.FILE_ID);
        this.parentId = httpHeaders.value(FileConstants.HeaderConstants.FILE_PARENT_ID);
    }
}
