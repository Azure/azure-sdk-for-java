// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

public class TestEncryptionBlob {
    private String key;
    private EncryptionData encryptionData;
    private String decryptedContent;
    private String encryptedContent;

    public TestEncryptionBlob() {}

    public TestEncryptionBlob(String key, EncryptionData encryptionData, String decryptedContent, String encryptedContent) {
        this.key = key;
        this.encryptionData = encryptionData;
        this.decryptedContent = decryptedContent;
        this.encryptedContent = encryptedContent;
    }

    public String getKey() { return this.key; }

    public EncryptionData getEncryptionData() { return this.encryptionData; }

    public String getEncryptedContent() { return this.encryptedContent; }

    public String getDecryptedContent() { return  this.decryptedContent; }
}
