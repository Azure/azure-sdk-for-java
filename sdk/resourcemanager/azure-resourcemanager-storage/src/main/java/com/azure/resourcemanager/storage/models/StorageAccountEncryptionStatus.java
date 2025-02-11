// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import java.time.OffsetDateTime;

/** Type representing the encryption status of a storage service. */
@Fluent
public interface StorageAccountEncryptionStatus {
    /**
     * Gets the storage service type.
     *
     * @return the storage service type
     */
    StorageService storageService();

    /**
     * Checks whether the encryption is enabled for the service false otherwise.
     *
     * @return true if the encryption is enabled for the service false otherwise
     */
    boolean isEnabled();

    /**
     * Gets rough estimate of the date/time when the encryption was last enabled.
     *
     * @return rough estimate of the date/time when the encryption was last enabled, null if the encryption is disabled
     */
    OffsetDateTime lastEnabledTime();

    /**
     * Gets the type of the key used to encrypt the storage service.
     *
     * @return the type of the key used to encrypt the storage service
     */
    KeyType keyType();
}
