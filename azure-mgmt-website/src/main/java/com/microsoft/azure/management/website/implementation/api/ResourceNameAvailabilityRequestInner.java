/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Resource name availability request content.
 */
public class ResourceNameAvailabilityRequestInner {
    /**
     * Resource name to verify.
     */
    private String name;

    /**
     * Resource type used for verification.
     */
    private String type;

    /**
     * Is fully qualified domain name.
     */
    private Boolean isFqdn;

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
     * @return the ResourceNameAvailabilityRequestInner object itself.
     */
    public ResourceNameAvailabilityRequestInner withName(String name) {
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
     * @return the ResourceNameAvailabilityRequestInner object itself.
     */
    public ResourceNameAvailabilityRequestInner withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the isFqdn value.
     *
     * @return the isFqdn value
     */
    public Boolean isFqdn() {
        return this.isFqdn;
    }

    /**
     * Set the isFqdn value.
     *
     * @param isFqdn the isFqdn value to set
     * @return the ResourceNameAvailabilityRequestInner object itself.
     */
    public ResourceNameAvailabilityRequestInner withIsFqdn(Boolean isFqdn) {
        this.isFqdn = isFqdn;
        return this;
    }

}
