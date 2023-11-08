// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.blob;

import com.azure.spring.cloud.service.implementation.storage.common.StorageProperties;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * Properties for Azure Storage Blob.
 */
public interface BlobServiceClientProperties extends StorageProperties {

    /**
     * Get the storage blob customer provided key.
     * @return the storage blob customer provided key.
     */
    String getCustomerProvidedKey();

    /**
     * Get the storage blob encryption scope.
     * @return the storage blob encryption scope.
     */
    String getEncryptionScope();

    /**
     * Get the storage blob service version.
     * @return the storage blob service version.
     */
    BlobServiceVersion getServiceVersion();

}
