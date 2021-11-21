// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import java.time.OffsetDateTime;

/** Type representing the encryption status of a storage service. */
@Fluent
public interface StorageAccountEncryptionStatus {
    /** @return the storage service type */
    StorageService storageService();

    /** @return true if the encryption is enabled for the service false otherwise */
    boolean isEnabled();

    /**
     * @return rough estimate of the date/time when the encryption was last enabled, null if the encryption is disabled
     */
    OffsetDateTime lastEnabledTime();

    /** @return the type of the key used to encrypt the storage service */
    KeyType keyType();
}
