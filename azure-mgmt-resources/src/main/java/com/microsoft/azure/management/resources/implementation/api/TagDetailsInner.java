/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import java.util.List;

/**
 * Tag details.
 */
public class TagDetailsInner {
    /**
     * Gets or sets the tag ID.
     */
    private String id;

    /**
     * Gets or sets the tag name.
     */
    private String tagName;

    /**
     * Gets or sets the tag count.
     */
    private TagCount count;

    /**
     * Gets or sets the list of tag values.
     */
    private List<TagValueInner> values;

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
     * @return the TagDetailsInner object itself.
     */
    public TagDetailsInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the tagName value.
     *
     * @return the tagName value
     */
    public String tagName() {
        return this.tagName;
    }

    /**
     * Set the tagName value.
     *
     * @param tagName the tagName value to set
     * @return the TagDetailsInner object itself.
     */
    public TagDetailsInner withTagName(String tagName) {
        this.tagName = tagName;
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
     * @return the TagDetailsInner object itself.
     */
    public TagDetailsInner withCount(TagCount count) {
        this.count = count;
        return this;
    }

    /**
     * Get the values value.
     *
     * @return the values value
     */
    public List<TagValueInner> values() {
        return this.values;
    }

    /**
     * Set the values value.
     *
     * @param values the values value to set
     * @return the TagDetailsInner object itself.
     */
    public TagDetailsInner withValues(List<TagValueInner> values) {
        this.values = values;
        return this;
    }

}
