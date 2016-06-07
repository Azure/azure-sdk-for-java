/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import java.util.List;

/**
 * Parameters of move resources.
 */
public class ResourcesMoveInfoInner {
    /**
     * Gets or sets the ids of the resources.
     */
    private List<String> resources;

    /**
     * The target resource group.
     */
    private String targetResourceGroup;

    /**
     * Get the resources value.
     *
     * @return the resources value
     */
    public List<String> resources() {
        return this.resources;
    }

    /**
     * Set the resources value.
     *
     * @param resources the resources value to set
     * @return the ResourcesMoveInfoInner object itself.
     */
    public ResourcesMoveInfoInner withResources(List<String> resources) {
        this.resources = resources;
        return this;
    }

    /**
     * Get the targetResourceGroup value.
     *
     * @return the targetResourceGroup value
     */
    public String targetResourceGroup() {
        return this.targetResourceGroup;
    }

    /**
     * Set the targetResourceGroup value.
     *
     * @param targetResourceGroup the targetResourceGroup value to set
     * @return the ResourcesMoveInfoInner object itself.
     */
    public ResourcesMoveInfoInner withTargetResourceGroup(String targetResourceGroup) {
        this.targetResourceGroup = targetResourceGroup;
        return this;
    }

}
