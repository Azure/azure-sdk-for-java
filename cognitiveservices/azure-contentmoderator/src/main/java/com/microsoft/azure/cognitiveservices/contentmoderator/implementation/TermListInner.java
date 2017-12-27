/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderator.TermListMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Term List  Properties.
 */
public class TermListInner {
    /**
     * Term list Id.
     */
    @JsonProperty(value = "Id")
    private Integer id;

    /**
     * Term list name.
     */
    @JsonProperty(value = "Name")
    private String name;

    /**
     * Description for term list.
     */
    @JsonProperty(value = "Description")
    private String description;

    /**
     * Term list metadata.
     */
    @JsonProperty(value = "Metadata")
    private TermListMetadata metadata;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public Integer id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the TermListInner object itself.
     */
    public TermListInner withId(Integer id) {
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
     * @return the TermListInner object itself.
     */
    public TermListInner withName(String name) {
        this.name = name;
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
     * @return the TermListInner object itself.
     */
    public TermListInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public TermListMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the TermListInner object itself.
     */
    public TermListInner withMetadata(TermListMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
