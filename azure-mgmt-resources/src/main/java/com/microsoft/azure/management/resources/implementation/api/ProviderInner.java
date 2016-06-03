/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import java.util.List;

/**
 * Resource provider information.
 */
public class ProviderInner {
    /**
     * Gets or sets the provider id.
     */
    private String id;

    /**
     * Gets or sets the namespace of the provider.
     */
    private String namespace;

    /**
     * Gets or sets the registration state of the provider.
     */
    private String registrationState;

    /**
     * Gets or sets the collection of provider resource types.
     */
    private List<ProviderResourceType> resourceTypes;

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
     * @return the ProviderInner object itself.
     */
    public ProviderInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the namespace value.
     *
     * @return the namespace value
     */
    public String namespace() {
        return this.namespace;
    }

    /**
     * Set the namespace value.
     *
     * @param namespace the namespace value to set
     * @return the ProviderInner object itself.
     */
    public ProviderInner withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Get the registrationState value.
     *
     * @return the registrationState value
     */
    public String registrationState() {
        return this.registrationState;
    }

    /**
     * Set the registrationState value.
     *
     * @param registrationState the registrationState value to set
     * @return the ProviderInner object itself.
     */
    public ProviderInner withRegistrationState(String registrationState) {
        this.registrationState = registrationState;
        return this;
    }

    /**
     * Get the resourceTypes value.
     *
     * @return the resourceTypes value
     */
    public List<ProviderResourceType> resourceTypes() {
        return this.resourceTypes;
    }

    /**
     * Set the resourceTypes value.
     *
     * @param resourceTypes the resourceTypes value to set
     * @return the ProviderInner object itself.
     */
    public ProviderInner withResourceTypes(List<ProviderResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
        return this;
    }

}
