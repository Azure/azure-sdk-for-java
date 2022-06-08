// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

/**
 * Specifies the version of the client side encryption protocol to use.
 * V1 uses AES/CBC which is no longer secure. It is supported for compatibility but should not be used for new uploads.
 */
public enum EncryptionVersion {
    /**
     * Version 1 of the client side encryption protocol. Deprecated for security reasons but supported for
     * compatibility.
     * @deprecated Use {@link #V2} instead
     */
    @Deprecated
    V1,

    /**
     * Version 2 of the client side encryption protocol.
     */
    V2
}
