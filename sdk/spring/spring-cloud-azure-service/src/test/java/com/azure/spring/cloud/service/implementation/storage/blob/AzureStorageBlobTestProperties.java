// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.blob;

import com.azure.spring.cloud.service.implementation.storage.AzureStorageTestProperties;
import com.azure.storage.blob.BlobServiceVersion;

public class AzureStorageBlobTestProperties extends AzureStorageTestProperties implements BlobServiceClientProperties {

    private String customerProvidedKey;
    private String encryptionScope;
    private BlobServiceVersion serviceVersion;
    private String containerName;
    private String blobName;

    @Override
    public String getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    public void setCustomerProvidedKey(String customerProvidedKey) {
        this.customerProvidedKey = customerProvidedKey;
    }

    @Override
    public String getEncryptionScope() {
        return encryptionScope;
    }

    public void setEncryptionScope(String encryptionScope) {
        this.encryptionScope = encryptionScope;
    }

    @Override
    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(BlobServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

}
