/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.apigeneration.Fluent;
import org.joda.time.DateTime;

/**
 * Type representing the encryption status of a storage service.
 */
@Fluent
public interface StorageAccountEncryptionStatus {
    /**
     * @return the storage service type
     */
    StorageService storageService();
    /**
     * @return true if the encryption is enabled for the service false otherwise
     */
    boolean isEnabled();
    /**
     * @return rough estimate of the date/time when the encryption was last enabled, null if
     * the encryption is disabled
     */
    DateTime lastEnabledTime();
}
