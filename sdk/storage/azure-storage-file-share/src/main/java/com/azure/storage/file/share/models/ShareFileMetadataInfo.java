// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.annotation.Immutable;

/**
 * This class contains the response information returned from the service when metadata is set on a file.
 */
@Immutable
public class ShareFileMetadataInfo {
    private final String eTag;
    private final Boolean isServerEncrypted;

    /**
     * Constructs a {@link ShareFileMetadataInfo}.
     *
     * @param eTag ETag of the file.
     * @param isServerEncrypted Flag indicating if the file's content is encrypted on the server.
     */
    public ShareFileMetadataInfo(final String eTag, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.isServerEncrypted = isServerEncrypted;
    }

    /**
     * @return the ETag of the file.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @return the encryption status of the file's content on the server.
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
