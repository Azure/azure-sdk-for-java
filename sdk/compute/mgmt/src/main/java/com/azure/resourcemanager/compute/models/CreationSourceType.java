// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

/** The source type of managed disk or snapshot. */
public enum CreationSourceType {
    /**
     * Indicates that disk or snapshot is created from OS disk image of a virtual machine platform or virtual machine
     * user image.
     */
    FROM_OS_DISK_IMAGE,
    /**
     * Indicates that disk or snapshot is created from data disk image of a virtual machine platform or virtual machine
     * user image.
     */
    FROM_DATA_DISK_IMAGE,
    /** Indicates that disk or snapshot is created by importing a blob in a storage account. */
    IMPORTED_FROM_VHD,
    /** Indicates that disk or snapshot is created by copying a snapshot. */
    COPIED_FROM_SNAPSHOT,
    /** Indicates that disk or snapshot is created by copying another managed disk. */
    COPIED_FROM_DISK,
    /** Indicates that disk or snapshot is created as empty disk. */
    EMPTY,
    /** Unknown source. */
    UNKNOWN
}
