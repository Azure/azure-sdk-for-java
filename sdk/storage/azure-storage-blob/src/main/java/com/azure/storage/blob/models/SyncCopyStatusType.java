// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// This file is not generated. The Blob TypeSpec does not describe it, so it is maintained by hand;
// edits here are preserved across regeneration.

package com.azure.storage.blob.models;

/**
 * Defines values for SyncCopyStatusType.
 */
public enum SyncCopyStatusType {
    /**
     * Enum value success.
     */
    SUCCESS("success");

    /**
     * The actual serialized value for a SyncCopyStatusType instance.
     */
    private final String value;

    SyncCopyStatusType(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a SyncCopyStatusType instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SyncCopyStatusType object, or null if unable to parse.
     */
    public static SyncCopyStatusType fromString(String value) {
        SyncCopyStatusType[] items = SyncCopyStatusType.values();
        for (SyncCopyStatusType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
