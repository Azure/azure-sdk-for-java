/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.CreateImageListMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Create Image List Response.
 */
public class CreateImageListInner {
    /**
     * Image List Id.
     */
    @JsonProperty(value = "id")
    private Double id;

    /**
     * Image List Name.
     */
    @JsonProperty(value = "name")
    private String name;

    /**
     * Description for image list.
     */
    @JsonProperty(value = "description")
    private String description;

    /**
     * Image List Metadata.
     */
    @JsonProperty(value = "metadata")
    private CreateImageListMetadata metadata;

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
     * @return the CreateImageListInner object itself.
     */
    public CreateImageListInner withId(Double id) {
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
     * @return the CreateImageListInner object itself.
     */
    public CreateImageListInner withName(String name) {
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
     * @return the CreateImageListInner object itself.
     */
    public CreateImageListInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public CreateImageListMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the CreateImageListInner object itself.
     */
    public CreateImageListInner withMetadata(CreateImageListMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
