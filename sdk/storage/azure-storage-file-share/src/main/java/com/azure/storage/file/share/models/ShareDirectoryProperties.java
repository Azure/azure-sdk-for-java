// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.FileSmbProperties;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contains properties information about a Directory in the storage File service.
 */
@Immutable
public final class ShareDirectoryProperties {
    private final Map<String, String> metadata;
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final boolean isServerEncrypted;
    private final FileSmbProperties smbProperties;

    /**
     * Creates an instance of properties information about a specific Directory.
     *
     * @param metadata A set of name-value pairs that contain metadata for the directory.
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely
     * encrypted using the specified algorithm. Otherwise, the value is set to false.
     * @param smbProperties The SMB properties of the directory.
     */
    public ShareDirectoryProperties(final Map<String, String> metadata, final String eTag,
        final OffsetDateTime lastModified, final boolean isServerEncrypted, final FileSmbProperties smbProperties) {
        this.metadata = metadata;
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
        this.smbProperties = smbProperties;
    }

    /**
     * @return A set of name-value pairs that contain metadata for the directory.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified
     * algorithm. Otherwise, the value is false.
     */
    public boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return The SMB Properties of the directory.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
