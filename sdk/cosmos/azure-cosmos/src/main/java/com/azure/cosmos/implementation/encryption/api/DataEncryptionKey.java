// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption.api;

public interface DataEncryptionKey {
    String getId();
    byte[] getRawKey();
    CosmosEncryptionAlgorithm getCosmosEncryptionAlgorithm();
    byte[] encryptData(byte[] plainText);
    byte[] decryptData(byte[] cipherText);
}
