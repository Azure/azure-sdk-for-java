/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * The VirtualApplication model.
 */
public class VirtualApplication {
    /**
     * The virtualPath property.
     */
    private String virtualPath;

    /**
     * The physicalPath property.
     */
    private String physicalPath;

    /**
     * The preloadEnabled property.
     */
    private Boolean preloadEnabled;

    /**
     * The virtualDirectories property.
     */
    private List<VirtualDirectory> virtualDirectories;

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
     * @return the VirtualApplication object itself.
     */
    public VirtualApplication withVirtualPath(String virtualPath) {
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
     * @return the VirtualApplication object itself.
     */
    public VirtualApplication withPhysicalPath(String physicalPath) {
        this.physicalPath = physicalPath;
        return this;
    }

    /**
     * Get the preloadEnabled value.
     *
     * @return the preloadEnabled value
     */
    public Boolean preloadEnabled() {
        return this.preloadEnabled;
    }

    /**
     * Set the preloadEnabled value.
     *
     * @param preloadEnabled the preloadEnabled value to set
     * @return the VirtualApplication object itself.
     */
    public VirtualApplication withPreloadEnabled(Boolean preloadEnabled) {
        this.preloadEnabled = preloadEnabled;
        return this;
    }

    /**
     * Get the virtualDirectories value.
     *
     * @return the virtualDirectories value
     */
    public List<VirtualDirectory> virtualDirectories() {
        return this.virtualDirectories;
    }

    /**
     * Set the virtualDirectories value.
     *
     * @param virtualDirectories the virtualDirectories value to set
     * @return the VirtualApplication object itself.
     */
    public VirtualApplication withVirtualDirectories(List<VirtualDirectory> virtualDirectories) {
        this.virtualDirectories = virtualDirectories;
        return this;
    }

}
