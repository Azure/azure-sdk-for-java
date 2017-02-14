/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

/**
 * Defines values for DiskVolumeTypes.
 */
public enum DiskVolumeTypes {
    /** Enum value OS. */
    OS("OS"),

    /** Enum value Data. */
    DATA("Data"),

    /** Enum value All. */
    ALL("All");

    /** The actual serialized value for a DiskVolumeTypes instance. */
    private String value;

    DiskVolumeTypes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}