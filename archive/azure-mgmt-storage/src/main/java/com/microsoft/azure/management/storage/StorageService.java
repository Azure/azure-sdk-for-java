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
 * Azure storage service types.
 */
@LangDefinition
public class StorageService extends ExpandableStringEnum<StorageService> {
    /** Static value Blob for StorageService. */
    public static final StorageService BLOB = fromString("Blob");

    /** Static value Table for StorageService. */
    public static final StorageService TABLE = fromString("Table");

    /** Static value Queue for StorageService. */
    public static final StorageService QUEUE = fromString("Queue");

    /**
     * Creates or finds a service service type based on its name.
     *
     * @param name a name to look for
     * @return an StorageService
     */
    public static StorageService fromString(String name) {
        return fromString(name, StorageService.class);
    }

    /**
     * @return known storage service types.
     */
    public static Collection<StorageService> values() {
        return values(StorageService.class);
    }
}
