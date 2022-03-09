// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.BlobClient;

/**
 * Defines values to indicate what strategy the SDK should use when reading from a blob to ensure the view of the data
 * is consistent and not changed during the read.
 * {@link #NONE}
 * {@link #ETAG}
 * {@link #VERSION_ID}
 */
public enum ConsistentReadControl {
    /**
     * No consistent read control. The client will honor user provided {@link BlobRequestConditions#getIfMatch()} and
     * {@link BlobClient#getVersionId()}.
     */
    NONE,

    /**
     * Default value. Consistent read control based on eTag.
     * If {@link BlobRequestConditions#getIfMatch()} is set, the client will honor this value.
     * Otherwise, {@link BlobRequestConditions#getIfMatch()} is set to the latest eTag.
     * Note: Modification of the base blob will result in an {@code IOException} or a {@code BlobStorageException} if
     * eTag is the only form of consistent read control being employed.
     */
    ETAG,

    /**
     * Consistent control based on versionId. Note: Versioning must be supported by the account to use this value.
     * If {@link BlobClient#getVersionId()} is set, the client will honor this value.
     * Otherwise, {@link BlobClient#getVersionId()} is set to the latest versionId.
     * Note: Modification of the base blob will not result in an {@code Exception} and allow you to continue reading the
     * entirety of the appropriate version of the blob determined at the time of opening the {@code InputStream} but it
     * may no longer be the latest data.
     */
    VERSION_ID
}
