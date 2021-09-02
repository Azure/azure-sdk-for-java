// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.CustomerProvidedKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Azure Storage Blob.
 */
@ConfigurationProperties(prefix = "spring.cloud.azure.storage.blob")
public class AzureStorageBlobProperties extends AzureStorageProperties {


    private CustomerProvidedKey customerProvidedKey;
    private String encryptionScope;
    private String endpoint;
    private BlobServiceVersion serviceVersion;

    public CustomerProvidedKey getCustomerProvidedKey() {
        return customerProvidedKey;
    }

    public void setCustomerProvidedKey(CustomerProvidedKey customerProvidedKey) {
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

}
