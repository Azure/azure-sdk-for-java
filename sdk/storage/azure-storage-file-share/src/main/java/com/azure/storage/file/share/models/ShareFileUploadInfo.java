// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;

import java.time.OffsetDateTime;

/**
 * This class contains the response information returned from the service when the file is uploaded.
 */
@Immutable
public final class ShareFileUploadInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final byte[] contentMd5;
    private final Boolean isServerEncrypted;

    /**
     * Constructs a {@link ShareFileUploadInfo}.
     *
     * @param eTag ETag of the file.
     * @param lastModified Datetime when the file was last modified.
     * @param contentMd5 MD5 of the file's content.
     * @param isServerEncrypted Flag indicating the encryption status of the file's content on the server.
     */
    public ShareFileUploadInfo(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * Gets the ETag of the file.
     *
     * @return the ETag of the file.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the time when the file was last modified.
     *
     * @return the time when the file was last modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the MD5 of the file's content.
     *
     * @return the MD5 of the file's content.
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Gets whether the file's content is encrypted on the server.
     *
     * @return whether the file's content is encrypted on the server.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
