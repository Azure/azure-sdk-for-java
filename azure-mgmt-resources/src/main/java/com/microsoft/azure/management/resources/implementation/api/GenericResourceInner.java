/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.microsoft.azure.Resource;

/**
 * Resource information.
 */
public class GenericResourceInner extends Resource {
    /**
     * Gets or sets the plan of the resource.
     */
    private Plan plan;

    /**
     * Gets or sets the resource properties.
     */
    private Object properties;

    /**
     * Get the plan value.
     *
     * @return the plan value
     */
    public Plan plan() {
        return this.plan;
    }

    /**
     * Set the plan value.
     *
     * @param plan the plan value to set
     * @return the GenericResourceInner object itself.
     */
    public GenericResourceInner withPlan(Plan plan) {
        this.plan = plan;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public Object properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the GenericResourceInner object itself.
     */
    public GenericResourceInner withProperties(Object properties) {
        this.properties = properties;
        return this;
    }

}
