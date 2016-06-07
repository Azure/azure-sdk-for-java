/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store firewall rule information.
 */
public class FirewallRuleInner {
    /**
     * Gets or sets the firewall rule's name.
     */
    private String name;

    /**
     * Gets the namespace and type of the firewall Rule.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String type;

    /**
     * Gets or sets the firewall rule's subscription ID.
     */
    private String id;

    /**
     * Gets or sets the firewall rule's regional location.
     */
    private String location;

    /**
     * Gets or sets the properties of the firewall rule.
     */
    private FirewallRuleProperties properties;

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
     * @return the FirewallRuleInner object itself.
     */
    public FirewallRuleInner withName(String name) {
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
     * @return the FirewallRuleInner object itself.
     */
    public FirewallRuleInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     * @return the FirewallRuleInner object itself.
     */
    public FirewallRuleInner withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public FirewallRuleProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the FirewallRuleInner object itself.
     */
    public FirewallRuleInner withProperties(FirewallRuleProperties properties) {
        this.properties = properties;
        return this;
    }

}
