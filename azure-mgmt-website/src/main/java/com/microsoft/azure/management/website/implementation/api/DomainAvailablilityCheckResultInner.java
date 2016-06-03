/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Domain availablility check result.
 */
public class DomainAvailablilityCheckResultInner {
    /**
     * Name of the domain.
     */
    private String name;

    /**
     * If true then domain can be purchased using CreateDomain Api.
     */
    private Boolean available;

    /**
     * Domain type. Possible values include: 'Regular', 'SoftDeleted'.
     */
    private DomainType domainType;

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
     * @return the DomainAvailablilityCheckResultInner object itself.
     */
    public DomainAvailablilityCheckResultInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the available value.
     *
     * @return the available value
     */
    public Boolean available() {
        return this.available;
    }

    /**
     * Set the available value.
     *
     * @param available the available value to set
     * @return the DomainAvailablilityCheckResultInner object itself.
     */
    public DomainAvailablilityCheckResultInner withAvailable(Boolean available) {
        this.available = available;
        return this;
    }

    /**
     * Get the domainType value.
     *
     * @return the domainType value
     */
    public DomainType domainType() {
        return this.domainType;
    }

    /**
     * Set the domainType value.
     *
     * @param domainType the domainType value to set
     * @return the DomainAvailablilityCheckResultInner object itself.
     */
    public DomainAvailablilityCheckResultInner withDomainType(DomainType domainType) {
        this.domainType = domainType;
        return this;
    }

}
