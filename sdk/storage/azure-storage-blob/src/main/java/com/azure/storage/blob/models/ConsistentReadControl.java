// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.BlobClient;

/**
 * Defines values for ConsistentReadControl.
 * {@link #NONE}
 * {@link #ETAG}
 * {@link #VERSION_ID}
 */
public enum ConsistentReadControl {
    /**
     * No consistent read control. 'requestConditions.ifMatch' and 'client.versionId' must not be set.
     */
    NONE,

    /**
     * Default value. Consistent read control based on eTag.
     * If {@link BlobRequestConditions#getIfMatch()} is set, the client will honor this value.
     * Otherwise, {@link BlobRequestConditions#getIfMatch()} is set to the latest eTag.
     * Note: Reading will fail if the underlying blob is changed (thus modifying its eTag).
     */
    ETAG,

    /**
     * Consistent control based on versionId. Note: Versioning must be supported by the account to use this value.
     * If {@link BlobClient#getVersionId()} is set, the client will honor this value.
     * Otherwise, {@link BlobClient#getVersionId()} is set to the latest versionId.
     * Note: Reading will succeed if the underlying blob is changed (thus modifying its version).
     */
    VERSION_ID
}
