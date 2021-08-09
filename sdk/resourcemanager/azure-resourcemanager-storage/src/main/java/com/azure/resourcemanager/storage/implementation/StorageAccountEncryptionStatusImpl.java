// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.storage.models.EncryptionService;
import com.azure.resourcemanager.storage.models.EncryptionServices;
import com.azure.resourcemanager.storage.models.KeyType;
import com.azure.resourcemanager.storage.models.StorageAccountEncryptionStatus;
import java.time.OffsetDateTime;

/** Shared implementation of StorageAccountEncryptionStatus. */
public abstract class StorageAccountEncryptionStatusImpl implements StorageAccountEncryptionStatus {
    protected final EncryptionServices encryptionServices;

    protected StorageAccountEncryptionStatusImpl(EncryptionServices encryptionServices) {
        this.encryptionServices = encryptionServices;
    }

    @Override
    public boolean isEnabled() {
        EncryptionService encryptionService = this.encryptionService();
        if (encryptionService == null || encryptionService.enabled() == null) {
            return true;
        } else {
            return encryptionService.enabled();
        }
    }

    @Override
    public OffsetDateTime lastEnabledTime() {
        EncryptionService encryptionService = this.encryptionService();
        if (encryptionService == null) {
            return null;
        } else {
            return encryptionService.lastEnabledTime();
        }
    }

    @Override
    public KeyType keyType() {
        EncryptionService encryptionService = this.encryptionService();
        if (encryptionService == null) {
            return null;
        } else {
            return encryptionService.keyType();
        }
    }

    protected abstract EncryptionService encryptionService();
}
