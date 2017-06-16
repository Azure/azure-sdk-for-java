/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * Azure storage service types.
 */
@LangDefinition
public class StorageService {
    /** Static value Blob for StorageService. */
    public static final StorageService BLOB = new StorageService("Blob");

    /** Static value Table for StorageService. */
    public static final StorageService TABLE = new StorageService("Table");

    /** Static value Queue for StorageService. */
    public static final StorageService QUEUE = new StorageService("Queue");

    private String value;

    /**
     * Creates a custom value for StorageService.
     * @param value the custom value
     */
    public StorageService(String value) {
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
        if (!(obj instanceof StorageService)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        StorageService rhs = (StorageService) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
