// ------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
// ------------------------------------------------------------

package com.azure.cosmos.encryption;

class EncryptionAlgorithm {
    private final DataEncryptionKey dataEncryptionKey;

    EncryptionAlgorithm(DataEncryptionKey dataEncryptionKey, EncryptionType type) {
        this.dataEncryptionKey = dataEncryptionKey;
    }

    byte[] encryptData(byte[] plainText) {
        return dataEncryptionKey.encryptData(plainText);
    }

    byte[] decryptData(byte[] encryptedData) {
        return dataEncryptionKey.decryptData(encryptedData);
    }
}
