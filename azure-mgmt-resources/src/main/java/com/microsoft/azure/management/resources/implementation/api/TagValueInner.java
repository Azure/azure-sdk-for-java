/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tag information.
 */
public class TagValueInner {
    /**
     * Gets or sets the tag ID.
     */
    private String id;

    /**
     * Gets or sets the tag value.
     */
    @JsonProperty(value = "tagValue")
    private String tagValueProperty;

    /**
     * Gets or sets the tag value count.
     */
    private TagCount count;

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
     * @return the TagValueInner object itself.
     */
    public TagValueInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the tagValueProperty value.
     *
     * @return the tagValueProperty value
     */
    public String tagValueProperty() {
        return this.tagValueProperty;
    }

    /**
     * Set the tagValueProperty value.
     *
     * @param tagValueProperty the tagValueProperty value to set
     * @return the TagValueInner object itself.
     */
    public TagValueInner withTagValueProperty(String tagValueProperty) {
        this.tagValueProperty = tagValueProperty;
        return this;
    }

    /**
     * Get the count value.
     *
     * @return the count value
     */
    public TagCount count() {
        return this.count;
    }

    /**
     * Set the count value.
     *
     * @param count the count value to set
     * @return the TagValueInner object itself.
     */
    public TagValueInner withCount(TagCount count) {
        this.count = count;
        return this;
    }

}
