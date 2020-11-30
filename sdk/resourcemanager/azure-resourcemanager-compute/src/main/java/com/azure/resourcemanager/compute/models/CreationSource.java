// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;

/** The source from which managed disk or snapshot is created. */
public class CreationSource {
    private final CreationData creationData;

    /**
     * Creates DiskSource.
     *
     * @param creationData the creation data of managed disk or snapshot
     */
    public CreationSource(CreationData creationData) {
        this.creationData = creationData;
    }

    /** @return type of the source from which disk or snapshot is created */
    public CreationSourceType type() {
        DiskCreateOption createOption = this.creationData.createOption();
        if (createOption == DiskCreateOption.FROM_IMAGE) {
            ImageDiskReference imageReference = this.creationData.imageReference();
            if (imageReference.lun() == null) {
                return CreationSourceType.FROM_OS_DISK_IMAGE;
            }
            return CreationSourceType.FROM_DATA_DISK_IMAGE;
        }
        if (createOption == DiskCreateOption.IMPORT) {
            return CreationSourceType.IMPORTED_FROM_VHD;
        }
        if (createOption == DiskCreateOption.COPY) {
            String sourceResourceId = this.creationData.sourceResourceId();
            if (sourceResourceId != null) {
                String resourceType = ResourceUtils.resourceTypeFromResourceId(sourceResourceId);
                if (resourceType.equalsIgnoreCase("disks")) {
                    return CreationSourceType.COPIED_FROM_DISK;
                }
                if (resourceType.equalsIgnoreCase("snapshots")) {
                    return CreationSourceType.COPIED_FROM_SNAPSHOT;
                }
            }
            if (this.creationData.sourceUri() != null) {
                sourceResourceId = this.creationData.sourceUri();
                String resourceType = ResourceUtils.resourceTypeFromResourceId(sourceResourceId);
                if (resourceType.equalsIgnoreCase("disks")) {
                    return CreationSourceType.COPIED_FROM_DISK;
                }
                if (resourceType.equalsIgnoreCase("snapshots")) {
                    return CreationSourceType.COPIED_FROM_SNAPSHOT;
                }
            }
        }
        if (createOption == DiskCreateOption.EMPTY) {
            return CreationSourceType.EMPTY;
        }
        return CreationSourceType.UNKNOWN;
    }

    /** @return ID of the source */
    public String sourceId() {
        if (this.type() == CreationSourceType.FROM_OS_DISK_IMAGE
            || this.type() == CreationSourceType.FROM_DATA_DISK_IMAGE) {
            return this.creationData.imageReference().id();
        }
        if (this.type() == CreationSourceType.IMPORTED_FROM_VHD) {
            return this.creationData.sourceUri();
        }
        if (this.type() == CreationSourceType.COPIED_FROM_DISK) {
            String sourceResourceId = this.creationData.sourceResourceId();
            if (sourceResourceId == null) {
                sourceResourceId = this.creationData.sourceUri();
            }
            return sourceResourceId;
        }
        if (this.type() == CreationSourceType.COPIED_FROM_SNAPSHOT) {
            String sourceResourceId = this.creationData.sourceResourceId();
            if (sourceResourceId == null) {
                sourceResourceId = this.creationData.sourceUri();
            }
            return sourceResourceId;
        }
        return null;
    }

    /**
     * @return the LUN value of the data disk image if this disk or snapshot is created from a data disk image, -1
     *     otherwise
     */
    public int sourceDataDiskImageLun() {
        if (this.type() == CreationSourceType.FROM_DATA_DISK_IMAGE) {
            return this.creationData.imageReference().lun();
        }
        return -1;
    }
}
