/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

import java.util.Collection;

/**
 * Azure storage account encryption key sources.
 */
@LangDefinition
public class StorageAccountEncryptionKeySource extends ExpandableStringEnum<StorageAccountEncryptionKeySource> {
    /** Static value Blob for StorageAccountEncryptionKeySource. */
    public static final StorageAccountEncryptionKeySource MICROSOFT_STORAGE = fromString("Microsoft.Storage");

    /**
     * Creates or finds an encryption status based on its name.
     *
     * @param name a name to look for
     * @return an StorageAccountEncryptionKeySource
     */
    public static StorageAccountEncryptionKeySource fromString(String name) {
        return fromString(name, StorageAccountEncryptionKeySource.class);
    }

    /**
     * @return known storage account encryption sources.
     */
    public static Collection<StorageAccountEncryptionKeySource> values() {
        return values(StorageAccountEncryptionKeySource.class);
    }
}
