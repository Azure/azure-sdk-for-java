// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.implementation;

import com.azure.management.storage.EncryptionService;
import com.azure.management.storage.EncryptionServices;
import com.azure.management.storage.StorageService;

/** Implementation of StorageAccountEncryptionStatus for File service. */
class FileServiceEncryptionStatusImpl extends StorageAccountEncryptionStatusImpl {
    FileServiceEncryptionStatusImpl(EncryptionServices encryptionServices) {
        super(encryptionServices);
    }

    @Override
    public StorageService storageService() {
        return StorageService.FILE;
    }

    @Override
    protected EncryptionService encryptionService() {
        if (super.encryptionServices == null) {
            return null;
        } else {
            return super.encryptionServices.file();
        }
    }
}
