// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Azure storage account encryption key sources. */
public class StorageAccountEncryptionKeySource extends ExpandableStringEnum<StorageAccountEncryptionKeySource> {
    /** Static value Microsoft.Storage for StorageAccountEncryptionKeySource. */
    public static final StorageAccountEncryptionKeySource MICROSOFT_STORAGE = fromString("Microsoft.Storage");
    /** Static value Microsoft.Keyvault for StorageAccountEncryptionKeySource. */
    public static final StorageAccountEncryptionKeySource MICROSOFT_KEYVAULT = fromString("Microsoft.Keyvault");

    /**
     * Creates or finds an encryption status based on its name.
     *
     * @param name a name to look for
     * @return an StorageAccountEncryptionKeySource
     */
    public static StorageAccountEncryptionKeySource fromString(String name) {
        return fromString(name, StorageAccountEncryptionKeySource.class);
    }

    /** @return known storage account encryption sources. */
    public static Collection<StorageAccountEncryptionKeySource> values() {
        return values(StorageAccountEncryptionKeySource.class);
    }
}
