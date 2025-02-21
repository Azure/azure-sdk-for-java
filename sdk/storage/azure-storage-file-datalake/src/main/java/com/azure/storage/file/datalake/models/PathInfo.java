// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

/**
 * {@code PathInfo} contains basic information about a path that is returned by the service after certain
 * operations.
 */
public class PathInfo {

    private final String eTag;
    private final OffsetDateTime lastModified;
    private final boolean isServerEncrypted;
    private final String encryptionKeySha256;

    /**
     * Constructs a {@link PathInfo}
     * @param eTag ETag of the path.
     * @param lastModified Datetime when the path was last modified.
     */
    public PathInfo(String eTag, OffsetDateTime lastModified) {
        this(eTag, lastModified, false, null);
    }

    /**
     * Constructs a {@link PathInfo}
     * @param eTag ETag of the path.
     * @param lastModified Datetime when the path was last modified.
     * @param isServerEncrypted Indicates whether the path is encrypted using Cpk.
     * @param encryptionKeySha256 An echo of the SHA256 of the key that was used to encrypt the data.
     */
    public PathInfo(String eTag, OffsetDateTime lastModified, boolean isServerEncrypted, String encryptionKeySha256) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.isServerEncrypted = isServerEncrypted;
        this.encryptionKeySha256 = encryptionKeySha256;
    }

    /**
     * Get the eTag property: The eTag property.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Get the last modified property: The last modified property.
     *
     * @return the time when the file was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * @return the encryption status of the block blob on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * @return the key used to encrypt the block blob
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }
}
