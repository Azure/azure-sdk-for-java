/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.CreateTermListMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Create Term List Response.
 */
public class CreateTermListInner {
    /**
     * Term list Id.
     */
    @JsonProperty(value = "id")
    private Double id;

    /**
     * Term list name.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Description for term list.
     */
    @JsonProperty(value = "description")
    private String description;

    /**
     * Term list metadata.
     */
    @JsonProperty(value = "metadata")
    private CreateTermListMetadata metadata;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public Double id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the CreateTermListInner object itself.
     */
    public CreateTermListInner withId(Double id) {
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
     * @return the CreateTermListInner object itself.
     */
    public CreateTermListInner withName(String name) {
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
     * @return the CreateTermListInner object itself.
     */
    public CreateTermListInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public CreateTermListMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the CreateTermListInner object itself.
     */
    public CreateTermListInner withMetadata(CreateTermListMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
