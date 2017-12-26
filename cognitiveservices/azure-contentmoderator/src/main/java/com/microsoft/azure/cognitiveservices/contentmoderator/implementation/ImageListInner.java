/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderator.ImageListMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Image List Properties.
 */
public class ImageListInner {
    /**
     * Image List Id.
     */
    @JsonProperty(value = "Id")
    private Integer id;

    /**
     * Image List Name.
     */
    @JsonProperty(value = "Name")
    private String name;

    /**
     * Description for image list.
     */
    @JsonProperty(value = "Description")
    private String description;

    /**
     * Image List Metadata.
     */
    @JsonProperty(value = "Metadata")
    private ImageListMetadata metadata;

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
     * @return the ImageListInner object itself.
     */
    public ImageListInner withId(Integer id) {
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
     * @return the ImageListInner object itself.
     */
    public ImageListInner withName(String name) {
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
     * @return the ImageListInner object itself.
     */
    public ImageListInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public ImageListMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the ImageListInner object itself.
     */
    public ImageListInner withMetadata(ImageListMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
