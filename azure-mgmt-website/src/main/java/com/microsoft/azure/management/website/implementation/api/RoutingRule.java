/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Routing rules for TiP.
 */
public class RoutingRule {
    /**
     * Name of the routing rule. The recommended name would be to point to the
     * slot which will receive the traffic in the experiment.
     */
    private String name;

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
     * @return the RoutingRule object itself.
     */
    public RoutingRule withName(String name) {
        this.name = name;
        return this;
    }

}
