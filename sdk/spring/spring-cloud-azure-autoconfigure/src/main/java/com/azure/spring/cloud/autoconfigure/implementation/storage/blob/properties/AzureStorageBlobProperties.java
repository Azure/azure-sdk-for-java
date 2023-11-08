// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties;

import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientProperties;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * Properties for Azure Storage Blob.
 */
public class AzureStorageBlobProperties extends AzureStorageProperties implements BlobServiceClientProperties {

    public static final String PREFIX = "spring.cloud.azure.storage.blob";

    private static final String BLOB_ENDPOINT_PATTERN = "https://%s.blob%s";

    /**
     * Customer-provided encryption key(base64 encoded string) used to encrypt blob contents on the server.
     * See Microsoft doc about Customer-provided Keys for more details: https://learn.microsoft.com/azure/storage/blobs/encryption-customer-provided-keys
     */
    private String customerProvidedKey;
    /**
     * Specifies the name of the encryption scope to use to encrypt the data provided in the request. If not specified, encryption is performed with the default account encryption scope.
     * See Microsoft doc about Encryption Scope for more details: https://learn.microsoft.com/azure/storage/blobs/encryption-scope-overview
     */
    private String encryptionScope;
    /**
     * Blob service version used when making API requests.
     */
    private BlobServiceVersion serviceVersion;
    /**
     * Name of the container.
     */
    private String containerName;
    /**
     * Name of the blob.
     */
    private String blobName;

    @Override
    public String getEndpoint() {
        return endpoint == null ? buildEndpointFromAccountName() : endpoint;
    }

    private String buildEndpointFromAccountName() {
        if (accountName == null || profile.getCloudType() == null) {
            return null;
        }
        return String.format(BLOB_ENDPOINT_PATTERN, accountName, profile.getEnvironment().getStorageEndpointSuffix());
    }

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
