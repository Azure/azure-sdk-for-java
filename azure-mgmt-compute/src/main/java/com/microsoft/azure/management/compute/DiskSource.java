/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * The source from which managed disk is created.
 */
@LangDefinition
public class DiskSource {
    private final Disk disk;

    /**
     * Creates DiskSource.
     *
     * @param disk the managed disk
     */
    public DiskSource(Disk disk) {
        this.disk = disk;
    }

    /**
     * @return type of the source from which disk is created
     */
    public DiskSourceType type() {
        DiskCreateOption createOption = this.disk.inner().creationData().createOption();
        if (createOption == DiskCreateOption.FROM_IMAGE) {
            ImageDiskReference imageReference = this.disk.inner().creationData().imageReference();
            if (imageReference.lun() == null) {
                return DiskSourceType.FROM_OS_DISK_IMAGE;
            }
            return DiskSourceType.FROM_DATA_DISK_IMAGE;
        }
        if (createOption == DiskCreateOption.IMPORT) {
            return DiskSourceType.IMPORTED_FROM_VHD;
        }
        if (createOption == DiskCreateOption.COPY) {
            String sourceResourceId = this.disk.inner().creationData().sourceResourceId();
            if (sourceResourceId != null) {
                String resourceType = ResourceUtils.resourceTypeFromResourceId(sourceResourceId);
                if (resourceType.equalsIgnoreCase("disks")) {
                    return DiskSourceType.COPIED_FROM_DISK;
                }
                if (resourceType.equalsIgnoreCase("snapshots")) {
                    return DiskSourceType.COPIED_FROM_SNAPSHOT;
                }
            }
            if (this.disk.inner().creationData().sourceUri() != null) {
                sourceResourceId = this.disk.inner().creationData().sourceUri();
                String resourceType = ResourceUtils.resourceTypeFromResourceId(sourceResourceId);
                if (resourceType.equalsIgnoreCase("disks")) {
                    return DiskSourceType.COPIED_FROM_DISK;
                }
                if (resourceType.equalsIgnoreCase("snapshots")) {
                    return DiskSourceType.COPIED_FROM_SNAPSHOT;
                }
            }
        }
        if (createOption == DiskCreateOption.EMPTY) {
            return DiskSourceType.EMPTY;
        }
        return DiskSourceType.UNKNOWN;
    }

    /**
     * @return id of the source
     */
    public String sourceId() {
        if (this.type() == DiskSourceType.FROM_OS_DISK_IMAGE
                || this.type() == DiskSourceType.FROM_DATA_DISK_IMAGE) {
            return this.disk.inner().creationData().imageReference().id();
        }
        if (this.type() == DiskSourceType.IMPORTED_FROM_VHD) {
            return this.disk.inner().creationData().sourceUri();
        }
        if (this.type() == DiskSourceType.COPIED_FROM_DISK) {
            String sourceResourceId = this.disk.inner().creationData().sourceResourceId();
            if (sourceResourceId == null) {
                sourceResourceId = this.disk.inner().creationData().sourceUri();
            }
            return sourceResourceId;
        }
        if (this.type() == DiskSourceType.COPIED_FROM_SNAPSHOT) {
            String sourceResourceId = this.disk.inner().creationData().sourceUri();
            if (sourceResourceId == null) {
                sourceResourceId = this.disk.inner().creationData().sourceUri();
            }
            return sourceResourceId;
        }
        return null;
    }

    /**
     * @return the lun value of the data disk image if this disk is created from
     * a data disk image, -1 otherwise
     */
    public int sourceDataDiskImageLun() {
        if (this.type() == DiskSourceType.FROM_DATA_DISK_IMAGE) {
            return this.disk.inner().creationData().imageReference().lun();
        }
        return -1;
    }
}
