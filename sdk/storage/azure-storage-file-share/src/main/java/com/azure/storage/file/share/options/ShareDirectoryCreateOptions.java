// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.models.FilePosixProperties;

import java.util.Map;

/**
 * Extended options that may be passed when creating a share directory.
 */
@Fluent
public class ShareDirectoryCreateOptions {
    private FileSmbProperties smbProperties;
    private String filePermission;
    private FilePermissionFormat filePermissionFormat;
    private Map<String, String> metadata;
    private FilePosixProperties posixProperties;
    // private FilePropertySemantics filePropertySemantics; PULLED FROM RELEASE

    /**
     * Creates a new instance of {@link ShareDirectoryCreateOptions}.
     */
    public ShareDirectoryCreateOptions() {
    }

    /**
     * Gets the file permission key.
     *
     * @return The file's permission key.
     */
    public String getFilePermission() {
        return filePermission;
    }

    /**
     * Sets the file permission key.
     *
     * @param filePermissionKey The file permission key.
     * @return the updated options.
     */
    public ShareDirectoryCreateOptions setFilePermission(String filePermissionKey) {
        this.filePermission = filePermissionKey;
        return this;
    }

    /**
     * Gets the SMB properties to set on the destination directory.
     *
     * @return Optional SMB properties to set on the destination file or directory. The only properties that are
     * considered are file attributes, file creation time, file last write time, and file permission key. The rest are
     * ignored.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }

    /**
     * Sets the SMB properties to set on the destination directory.
     *
     * @param smbProperties Optional SMB properties to set on the destination file or directory. The only properties
     * that are  considered are file attributes, file creation time, file last write time, and file permission key. The
     * rest are ignored.
     * @return The updated options.
     */
    public ShareDirectoryCreateOptions setSmbProperties(FileSmbProperties smbProperties) {
        this.smbProperties = smbProperties;
        return this;
    }

    /**
     * Gets the metadata to associate with the directory.
     *
     * @return Metadata to associate with the directory.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the directory.
     *
     * @param metadata Metadata to associate with the directory. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public ShareDirectoryCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the file permission format.
     *
     * @return file permission format.
     */
    public FilePermissionFormat getFilePermissionFormat() {
        return filePermissionFormat;
    }

    /**
     * Sets the file permission format.
     *
     * @param filePermissionFormat the file permission format.
     * @return The updated options.
     */
    public ShareDirectoryCreateOptions setFilePermissionFormat(FilePermissionFormat filePermissionFormat) {
        this.filePermissionFormat = filePermissionFormat;
        return this;
    }

    /**
     *  Optional properties to set on NFS directories.
     *  Note that this property is only applicable to directories created in NFS shares.
     *
     * @return {@link FilePosixProperties}
     */
    public FilePosixProperties getPosixProperties() {
        return posixProperties;
    }

    /**
     *  Optional properties to set on NFS directories.
     *  Note that this property is only applicable to directories created in NFS shares.
     *
     * @param posixProperties {@link FilePosixProperties}
     * @return The updated options.
     */
    public ShareDirectoryCreateOptions setPosixProperties(FilePosixProperties posixProperties) {
        this.posixProperties = posixProperties;
        return this;
    }

    /* PULLED FROM RELEASE
     * Optional, only applicable to SMB directories. Gets how attributes and permissions should be set on the file.
     * New: automatically adds the ARCHIVE file attribute flag to the file and uses Windows create file permissions
     * semantics (ex: inherit from parent).
     * Restore: does not modify file attribute flag and uses Windows update file permissions semantics.
     * If Restore is specified, the file permission must also be provided, otherwise PropertySemantics will default to New.
     *
     * @return {@link FilePropertySemantics}
    
    public FilePropertySemantics getFilePropertySemantics() {
        return filePropertySemantics;
    }
    
    /**
     * Optional, only applicable to SMB directories. Sets how attributes and permissions should be set on the file.
     * New: automatically adds the ARCHIVE file attribute flag to the file and uses Windows create file permissions
     * semantics (ex: inherit from parent).
     * Restore: does not modify file attribute flag and uses Windows update file permissions semantics.
     * If Restore is specified, the file permission must also be provided, otherwise PropertySemantics will default to New.
     *
     * @param filePropertySemantics {@link FilePropertySemantics}
     * @return The updated options.
    
    public ShareDirectoryCreateOptions setFilePropertySemantics(FilePropertySemantics filePropertySemantics) {
        this.filePropertySemantics = filePropertySemantics;
        return this;
    } */
}
