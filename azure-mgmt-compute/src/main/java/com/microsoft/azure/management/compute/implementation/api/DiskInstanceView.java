/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * The instance view of the disk.
 */
public class DiskInstanceView {
    /**
     * Gets or sets the disk name.
     */
    private String name;

    /**
     * Gets or sets the resource status information.
     */
    private List<InstanceViewStatus> statuses;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the DiskInstanceView object itself.
     */
    public DiskInstanceView withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the statuses value.
     *
     * @return the statuses value
     */
    public List<InstanceViewStatus> statuses() {
        return this.statuses;
    }

    /**
     * Set the statuses value.
     *
     * @param statuses the statuses value to set
     * @return the DiskInstanceView object itself.
     */
    public DiskInstanceView withStatuses(List<InstanceViewStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

}
