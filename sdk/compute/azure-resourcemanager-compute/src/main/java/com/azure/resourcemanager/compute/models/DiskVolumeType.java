// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

/** Defines values for DiskVolumeType. */
public enum DiskVolumeType {
    /** Enum value OS. */
    OS("OS"),

    /** Enum value Data. */
    DATA("Data"),

    /** Enum value All. */
    ALL("All");

    /** The actual serialized value for a DiskVolumeTypes instance. */
    private String value;

    DiskVolumeType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
