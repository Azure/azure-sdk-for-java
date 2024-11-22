// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

/**
 * Specifies the version of the client side encryption protocol to use.
 */
public enum EncryptionVersion {
    /**
     * Version 1 of the client side encryption protocol. Deprecated for security reasons but supported for
     * compatibility. Should not be used for new uploads.
     * Uses AES/CBC/PKCS5
     * @deprecated Use {@link #V2} instead
     */
    @Deprecated
    V1,

    /**
     * Version 2 of the client side encryption protocol.
     * Uses AES/GCM/NoPadding
     */
    V2,

    /**
     * Version 2.1 of the client side encryption protocol. Use this version when configuring {@link BlobClientSideEncryptionOptions}
     * authenticatedRegionDataLength for encryption.
     * Uses AES/GCM/NoPadding
     */
    V2_1
}
