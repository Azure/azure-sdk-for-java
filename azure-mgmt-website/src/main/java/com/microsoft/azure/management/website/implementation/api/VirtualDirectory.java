/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * The VirtualDirectory model.
 */
public class VirtualDirectory {
    /**
     * The virtualPath property.
     */
    private String virtualPath;

    /**
     * The physicalPath property.
     */
    private String physicalPath;

    /**
     * Get the virtualPath value.
     *
     * @return the virtualPath value
     */
    public String virtualPath() {
        return this.virtualPath;
    }

    /**
     * Set the virtualPath value.
     *
     * @param virtualPath the virtualPath value to set
     * @return the VirtualDirectory object itself.
     */
    public VirtualDirectory withVirtualPath(String virtualPath) {
        this.virtualPath = virtualPath;
        return this;
    }

    /**
     * Get the physicalPath value.
     *
     * @return the physicalPath value
     */
    public String physicalPath() {
        return this.physicalPath;
    }

    /**
     * Set the physicalPath value.
     *
     * @param physicalPath the physicalPath value to set
     * @return the VirtualDirectory object itself.
     */
    public VirtualDirectory withPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
        return this;
    }

}
