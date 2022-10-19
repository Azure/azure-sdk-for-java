// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

enum EncryptionAlgorithm {
    /**
     * AES-CBC using a 256 bit key.
     */
    AES_CBC_256,

    /**
     * AES-GCM using a 256 bit key.
     */
    AES_GCM_256
}
