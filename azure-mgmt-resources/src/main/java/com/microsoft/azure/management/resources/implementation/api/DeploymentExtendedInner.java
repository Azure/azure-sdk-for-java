/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Deployment information.
 */
public class DeploymentExtendedInner {
    /**
     * Gets or sets the ID of the deployment.
     */
    private String id;

    /**
     * Gets or sets the name of the deployment.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets deployment properties.
     */
    private DeploymentPropertiesExtended properties;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the DeploymentExtendedInner object itself.
     */
    public DeploymentExtendedInner withId(String id) {
        this.id = id;
        return this;
    }

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
     * @return the DeploymentExtendedInner object itself.
     */
    public DeploymentExtendedInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public DeploymentPropertiesExtended properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the DeploymentExtendedInner object itself.
     */
    public DeploymentExtendedInner withProperties(DeploymentPropertiesExtended properties) {
        this.properties = properties;
        return this;
    }

}
