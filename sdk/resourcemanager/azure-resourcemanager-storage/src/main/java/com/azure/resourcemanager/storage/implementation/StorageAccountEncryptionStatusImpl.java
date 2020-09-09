// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.storage.models.EncryptionService;
import com.azure.resourcemanager.storage.models.EncryptionServices;
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
        if (encryptionService == null) {
            return false;
        } else if (encryptionService.enabled() != null) {
            return encryptionService.enabled();
        } else {
            return false;
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

    protected abstract EncryptionService encryptionService();
}
