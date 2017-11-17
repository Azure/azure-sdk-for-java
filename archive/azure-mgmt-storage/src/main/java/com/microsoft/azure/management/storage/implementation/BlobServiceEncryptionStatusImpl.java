/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.storage.EncryptionServices;
import com.microsoft.azure.management.storage.StorageAccountEncryptionStatus;
import com.microsoft.azure.management.storage.StorageService;
import org.joda.time.DateTime;

/**
 * Implementation of StorageAccountEncryptionStatus for Blob service.
 */
@LangDefinition
class BlobServiceEncryptionStatusImpl implements StorageAccountEncryptionStatus {
    private final EncryptionServices encryptionServices;

    BlobServiceEncryptionStatusImpl(EncryptionServices encryptionServices) {
        this.encryptionServices = encryptionServices;
    }

    @Override
    public StorageService storageService() {
        return StorageService.BLOB;
    }

    @Override
    public boolean isEnabled() {
        if (this.encryptionServices.blob() != null
                && this.encryptionServices.blob().enabled() != null) {
            return this.encryptionServices.blob().enabled();
        }
        return false;
    }

    @Override
    public DateTime lastEnabledTime() {
        if (this.encryptionServices.blob() != null) {
            return this.encryptionServices.blob().lastEnabledTime();
        }
        return null;
    }
}
