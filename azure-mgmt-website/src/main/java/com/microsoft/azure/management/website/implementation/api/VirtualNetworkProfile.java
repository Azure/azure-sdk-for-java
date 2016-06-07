/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Specification for using a virtual network.
 */
public class VirtualNetworkProfile {
    /**
     * Resource id of the virtual network.
     */
    private String id;

    /**
     * Name of the virtual network (read-only).
     */
    private String name;

    /**
     * Resource type of the virtual network (read-only).
     */
    private String type;

    /**
     * Subnet within the virtual network.
     */
    private String subnet;

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
     * @return the VirtualNetworkProfile object itself.
     */
    public VirtualNetworkProfile withId(String id) {
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
     * @return the VirtualNetworkProfile object itself.
     */
    public VirtualNetworkProfile withName(String name) {
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
     * @return the VirtualNetworkProfile object itself.
     */
    public VirtualNetworkProfile withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the subnet value.
     *
     * @return the subnet value
     */
    public String subnet() {
        return this.subnet;
    }

    /**
     * Set the subnet value.
     *
     * @param subnet the subnet value to set
     * @return the VirtualNetworkProfile object itself.
     */
    public VirtualNetworkProfile withSubnet(String subnet) {
        this.subnet = subnet;
        return this;
    }

}
