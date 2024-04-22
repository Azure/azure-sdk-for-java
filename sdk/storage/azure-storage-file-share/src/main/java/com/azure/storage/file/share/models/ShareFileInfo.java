// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.storage.file.share.FileSmbProperties;

import java.time.OffsetDateTime;

/**
 * Contains information about a File in the storage File service.
 */
@Immutable
public final class ShareFileInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Boolean isServerEncrypted;
    private final FileSmbProperties smbProperties;

    /**
     * Creates an instance of information about a specific Directory.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param isServerEncrypted The value of this header is set to true if the directory metadata is completely
     * encrypted using the specified algorithm. Otherwise, the value is set to false.
     * @param smbProperties The SMB properties of the file.
     */
    public ShareFileInfo(String eTag, OffsetDateTime lastModified, Boolean isServerEncrypted,
        FileSmbProperties smbProperties) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
        this.smbProperties = smbProperties;
    }

    /**
     * Gets the entity tag that corresponds to the file.
     *
     * @return The entity tag that corresponds to the file.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the last time the file was modified.
     *
     * @return The last time the file was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * The value of this header is true if the file metadata is completely encrypted using the specified algorithm.
     * Otherwise, the value is false.
     *
     * @return The value of this header is true if the file metadata is completely encrypted using the specified
     * algorithm. Otherwise, the value is false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the SMB Properties of the file.
     *
     * @return The SMB Properties of the file.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
