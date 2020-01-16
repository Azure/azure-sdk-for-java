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
    public ShareFileInfo(final String eTag, final OffsetDateTime lastModified, final Boolean isServerEncrypted,
        final FileSmbProperties smbProperties) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
        this.smbProperties = smbProperties;
    }

    /**
     * @return The entity tag that corresponds to the directory.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return The last time the share was modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return The value of this header is true if the directory metadata is completely encrypted using the specified
     * algorithm. Otherwise, the value is false.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return The SMB Properties of the file.
     */
    public FileSmbProperties getSmbProperties() {
        return smbProperties;
    }
}
