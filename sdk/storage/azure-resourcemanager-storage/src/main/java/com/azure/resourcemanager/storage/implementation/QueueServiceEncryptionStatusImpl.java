// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.storage.models.EncryptionService;
import com.azure.resourcemanager.storage.models.EncryptionServices;
import com.azure.resourcemanager.storage.models.StorageService;

/** Implementation of StorageAccountEncryptionStatus for Queue service. */
class QueueServiceEncryptionStatusImpl extends StorageAccountEncryptionStatusImpl {
    QueueServiceEncryptionStatusImpl(EncryptionServices encryptionServices) {
        super(encryptionServices);
    }

    @Override
    public StorageService storageService() {
        return StorageService.QUEUE;
    }

    @Override
    protected EncryptionService encryptionService() {
        if (super.encryptionServices == null) {
            return null;
        } else {
            return super.encryptionServices.queue();
        }
    }
}
