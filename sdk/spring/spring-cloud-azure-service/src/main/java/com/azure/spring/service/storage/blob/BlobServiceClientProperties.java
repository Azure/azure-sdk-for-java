// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.blob;

import com.azure.spring.service.storage.common.StorageProperties;
import com.azure.storage.blob.BlobServiceVersion;

/**
 * Properties for Azure Storage Blob.
 */
public interface BlobServiceClientProperties extends StorageProperties {

    String getCustomerProvidedKey();

    String getEncryptionScope();

    BlobServiceVersion getServiceVersion();

}
