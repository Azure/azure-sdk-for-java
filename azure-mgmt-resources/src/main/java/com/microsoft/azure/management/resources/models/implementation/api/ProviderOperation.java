/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.implementation.api;


/**
 * Operation.
 */
public class ProviderOperation {
    /**
     * Gets or sets the operation name.
     */
    private String name;

    /**
     * Gets or sets the operation display name.
     */
    private String displayName;

    /**
     * Gets or sets the operation description.
     */
    private String description;

    /**
     * Gets or sets the operation origin.
     */
    private String origin;

    /**
     * Gets or sets the operation properties.
     */
    private Object properties;

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
     * @return the ProviderOperation object itself.
     */
    public ProviderOperation setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the ProviderOperation object itself.
     */
    public ProviderOperation setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the ProviderOperation object itself.
     */
    public ProviderOperation setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the origin value.
     *
     * @return the origin value
     */
    public String origin() {
        return this.origin;
    }

    /**
     * Set the origin value.
     *
     * @param origin the origin value to set
     * @return the ProviderOperation object itself.
     */
    public ProviderOperation setOrigin(String origin) {
        this.origin = origin;
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
     * @return the ProviderOperation object itself.
     */
    public ProviderOperation setProperties(Object properties) {
        this.properties = properties;
        return this;
    }

}
