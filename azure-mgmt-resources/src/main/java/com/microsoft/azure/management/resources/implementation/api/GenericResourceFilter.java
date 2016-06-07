/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Resource filter.
 */
public class GenericResourceFilter {
    /**
     * Gets or sets the resource type.
     */
    private String resourceType;

    /**
     * Gets or sets the tag name.
     */
    private String tagname;

    /**
     * Gets or sets the tag value.
     */
    private String tagvalue;

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
     * @return the GenericResourceFilter object itself.
     */
    public GenericResourceFilter withResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    /**
     * Get the tagname value.
     *
     * @return the tagname value
     */
    public String tagname() {
        return this.tagname;
    }

    /**
     * Set the tagname value.
     *
     * @param tagname the tagname value to set
     * @return the GenericResourceFilter object itself.
     */
    public GenericResourceFilter withTagname(String tagname) {
        this.tagname = tagname;
        return this;
    }

    /**
     * Get the tagvalue value.
     *
     * @return the tagvalue value
     */
    public String tagvalue() {
        return this.tagvalue;
    }

    /**
     * Set the tagvalue value.
     *
     * @param tagvalue the tagvalue value to set
     * @return the GenericResourceFilter object itself.
     */
    public GenericResourceFilter withTagvalue(String tagvalue) {
        this.tagvalue = tagvalue;
        return this;
    }

}
