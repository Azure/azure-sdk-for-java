// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Azure storage service types. */
public class StorageService extends ExpandableStringEnum<StorageService> {
    /** Static value Blob for StorageService. */
    public static final StorageService BLOB = fromString("Blob");

    /** Static value Table for StorageService. */
    public static final StorageService TABLE = fromString("Table");

    /** Static value Queue for StorageService. */
    public static final StorageService QUEUE = fromString("Queue");

    /** Static value File for StorageService. */
    public static final StorageService FILE = fromString("File");

    /**
     * Creates or finds a service service type based on its name.
     *
     * @param name a name to look for
     * @return an StorageService
     */
    public static StorageService fromString(String name) {
        return fromString(name, StorageService.class);
    }

    /** @return known storage service types. */
    public static Collection<StorageService> values() {
        return values(StorageService.class);
    }
}
