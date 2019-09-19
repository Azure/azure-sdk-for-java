// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.implementation.util.ImplUtils;

import java.time.OffsetDateTime;

public final class FileUploadInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMD5;
    private final Boolean isServerEncrypted;

    public FileUploadInfo(final String eTag, final OffsetDateTime lastModified, final byte[] contentMD5,
        final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMD5 = ImplUtils.clone(contentMD5);
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return the ETag of the file.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the time when the file was last modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the MD5 of the file's content.
     */
    public byte[] getContentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    /**
     * @return whether the file's content is encrypted on the server.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
