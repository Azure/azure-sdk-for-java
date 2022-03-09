// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.storage.models.EncryptionService;
import com.azure.resourcemanager.storage.models.EncryptionServices;
import com.azure.resourcemanager.storage.models.StorageService;

/** Implementation of StorageAccountEncryptionStatus for Table service. */
class TableServiceEncryptionStatusImpl extends StorageAccountEncryptionStatusImpl {
    TableServiceEncryptionStatusImpl(EncryptionServices encryptionServices) {
        super(encryptionServices);
    }

    @Override
    public StorageService storageService() {
        return StorageService.TABLE;
    }

    @Override
    protected EncryptionService encryptionService() {
        if (super.encryptionServices == null) {
            return null;
        } else {
            return super.encryptionServices.table();
        }
    }
}
