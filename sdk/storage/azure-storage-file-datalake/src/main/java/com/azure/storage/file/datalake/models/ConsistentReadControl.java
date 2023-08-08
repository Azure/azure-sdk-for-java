// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines values to indicate what strategy the SDK should use when reading from a blob to ensure the view of the data
 * is consistent and not changed during the read.
 */
public enum ConsistentReadControl {
    /**
     * No consistent read control. The client will honor user provided {@link DataLakeRequestConditions#getIfMatch()}
     */
    NONE,

    /**
     * Default value. Consistent read control based on eTag.
     * If {@link DataLakeRequestConditions#getIfMatch()} is set, the client will honor this value.
     * Otherwise, {@link DataLakeRequestConditions#getIfMatch()} is set to the latest eTag.
     * Note: Modification of the base blob will result in an {@code IOException} or a {@code BlobStorageException} if
     * eTag is the only form of consistent read control being employed.
     */
    ETAG,
}
