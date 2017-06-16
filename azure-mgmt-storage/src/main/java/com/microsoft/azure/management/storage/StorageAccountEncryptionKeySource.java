/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Azure storage account encryption key sources.
 */
@LangDefinition
public class StorageAccountEncryptionKeySource {
    /** Static value Blob for StorageAccountEncryptionKeySource. */
    public static final StorageAccountEncryptionKeySource MICROSOFT_STORAGE = new StorageAccountEncryptionKeySource("Microsoft.Storage");

    private String value;

    /**
     * Creates a custom value for StorageAccountEncryptionKeySource.
     * @param value the custom value
     */
    public StorageAccountEncryptionKeySource(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StorageAccountEncryptionKeySource)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        StorageAccountEncryptionKeySource rhs = (StorageAccountEncryptionKeySource) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
