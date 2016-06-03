/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Specification for a hostingEnvironment (App Service Environment) to use for
 * this resource.
 */
public class HostingEnvironmentProfile {
    /**
     * Resource id of the hostingEnvironment (App Service Environment).
     */
    private String id;

    /**
     * Name of the hostingEnvironment (App Service Environment) (read only).
     */
    private String name;

    /**
     * Resource type of the hostingEnvironment (App Service Environment) (read
     * only).
     */
    private String type;

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
     * @return the HostingEnvironmentProfile object itself.
     */
    public HostingEnvironmentProfile withId(String id) {
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
     * @return the HostingEnvironmentProfile object itself.
     */
    public HostingEnvironmentProfile withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the HostingEnvironmentProfile object itself.
     */
    public HostingEnvironmentProfile withType(String type) {
        this.type = type;
        return this;
    }

}
