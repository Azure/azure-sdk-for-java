/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import java.util.List;
import java.util.Map;

/**
 * Resource type managed by the resource provider.
 */
public class ProviderResourceType {
    /**
     * Gets or sets the resource type.
     */
    private String resourceType;

    /**
     * Gets or sets the collection of locations where this resource type can
     * be created in.
     */
    private List<String> locations;

    /**
     * Gets or sets the api version.
     */
    private List<String> apiVersions;

    /**
     * Gets or sets the properties.
     */
    private Map<String, String> properties;

    /**
     * Get the resourceType value.
     *
     * @return the resourceType value
     */
    public String resourceType() {
        return this.resourceType;
    }

    /**
     * Set the resourceType value.
     *
     * @param resourceType the resourceType value to set
     * @return the ProviderResourceType object itself.
     */
    public ProviderResourceType withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    /**
     * Get the locations value.
     *
     * @return the locations value
     */
    public List<String> locations() {
        return this.locations;
    }

    /**
     * Set the locations value.
     *
     * @param locations the locations value to set
     * @return the ProviderResourceType object itself.
     */
    public ProviderResourceType withLocations(List<String> locations) {
        this.locations = locations;
        return this;
    }

    /**
     * Get the apiVersions value.
     *
     * @return the apiVersions value
     */
    public List<String> apiVersions() {
        return this.apiVersions;
    }

    /**
     * Set the apiVersions value.
     *
     * @param apiVersions the apiVersions value to set
     * @return the ProviderResourceType object itself.
     */
    public ProviderResourceType withApiVersions(List<String> apiVersions) {
        this.apiVersions = apiVersions;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public Map<String, String> properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the ProviderResourceType object itself.
     */
    public ProviderResourceType withProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

}
