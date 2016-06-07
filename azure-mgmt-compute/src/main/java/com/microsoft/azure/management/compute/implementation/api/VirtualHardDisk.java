/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Describes the uri of a disk.
 */
public class VirtualHardDisk {
    /**
     * Gets or sets the virtual hard disk's uri. It should be a valid Uri to a
     * virtual hard disk.
     */
    private String uri;

    /**
     * Get the uri value.
     *
     * @return the uri value
     */
    public String uri() {
        return this.uri;
    }

    /**
     * Set the uri value.
     *
     * @param uri the uri value to set
     * @return the VirtualHardDisk object itself.
     */
    public VirtualHardDisk withUri(String uri) {
        this.uri = uri;
        return this;
    }

}
