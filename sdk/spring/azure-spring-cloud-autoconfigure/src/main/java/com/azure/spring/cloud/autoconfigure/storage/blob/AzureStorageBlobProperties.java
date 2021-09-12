// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * Properties for Azure Storage Blob.
 */
public class AzureStorageBlobProperties extends AzureStorageProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.blob";

    private String customerProvidedKey;
    private String encryptionScope;
    private String endpoint;
    private BlobServiceVersion serviceVersion;

    private String containerName;
    private String blobName;

    public String getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    public void setCustomerProvidedKey(String customerProvidedKey) {
        this.customerProvidedKey = customerProvidedKey;
    }

    public String getEncryptionScope() {
        return encryptionScope;
    }

    public void setEncryptionScope(String encryptionScope) {
        this.encryptionScope = encryptionScope;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(BlobServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

}
