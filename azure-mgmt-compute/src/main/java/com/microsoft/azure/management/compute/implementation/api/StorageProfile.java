/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * Describes a storage profile.
 */
public class StorageProfile {
    /**
     * Gets or sets the image reference.
     */
    private ImageReference imageReference;

    /**
     * Gets or sets the OS disk.
     */
    private OSDisk osDisk;

    /**
     * Gets or sets the data disks.
     */
    private List<DataDisk> dataDisks;

    /**
     * Get the imageReference value.
     *
     * @return the imageReference value
     */
    public ImageReference imageReference() {
        return this.imageReference;
    }

    /**
     * Set the imageReference value.
     *
     * @param imageReference the imageReference value to set
     * @return the StorageProfile object itself.
     */
    public StorageProfile withImageReference(ImageReference imageReference) {
        this.imageReference = imageReference;
        return this;
    }

    /**
     * Get the osDisk value.
     *
     * @return the osDisk value
     */
    public OSDisk osDisk() {
        return this.osDisk;
    }

    /**
     * Set the osDisk value.
     *
     * @param osDisk the osDisk value to set
     * @return the StorageProfile object itself.
     */
    public StorageProfile withOsDisk(OSDisk osDisk) {
        this.osDisk = osDisk;
        return this;
    }

    /**
     * Get the dataDisks value.
     *
     * @return the dataDisks value
     */
    public List<DataDisk> dataDisks() {
        return this.dataDisks;
    }

    /**
     * Set the dataDisks value.
     *
     * @param dataDisks the dataDisks value to set
     * @return the StorageProfile object itself.
     */
    public StorageProfile withDataDisks(List<DataDisk> dataDisks) {
        this.dataDisks = dataDisks;
        return this;
    }

}
