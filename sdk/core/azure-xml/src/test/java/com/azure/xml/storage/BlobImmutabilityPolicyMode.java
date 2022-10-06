// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml.storage;

/** Defines values for BlobImmutabilityPolicyMode. */
public enum BlobImmutabilityPolicyMode {
    /** Enum value Mutable. */
    MUTABLE("Mutable"),

    /** Enum value Unlocked. */
    UNLOCKED("Unlocked"),

    /** Enum value Locked. */
    LOCKED("Locked");

    /** The actual serialized value for a BlobImmutabilityPolicyMode instance. */
    private final String value;

    BlobImmutabilityPolicyMode(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a BlobImmutabilityPolicyMode instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed BlobImmutabilityPolicyMode object, or null if unable to parse.
     */
    public static BlobImmutabilityPolicyMode fromString(String value) {
        BlobImmutabilityPolicyMode[] items = BlobImmutabilityPolicyMode.values();
        for (BlobImmutabilityPolicyMode item : items) {
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
