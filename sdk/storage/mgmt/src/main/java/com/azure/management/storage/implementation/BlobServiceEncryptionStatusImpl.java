/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.storage.EncryptionService;
import com.azure.management.storage.EncryptionServices;
import com.azure.management.storage.StorageService;

/**
 * Implementation of StorageAccountEncryptionStatus for Blob service.
 */
class BlobServiceEncryptionStatusImpl extends StorageAccountEncryptionStatusImpl {
    BlobServiceEncryptionStatusImpl(EncryptionServices encryptionServices) {
        super(encryptionServices);
    }

    @Override
    public StorageService storageService() {
        return StorageService.BLOB;
    }

    @Override
    protected EncryptionService encryptionService() {
        if (super.encryptionServices == null) {
            return null;
        } else {
            return super.encryptionServices.getBlob();
        }
    }
}