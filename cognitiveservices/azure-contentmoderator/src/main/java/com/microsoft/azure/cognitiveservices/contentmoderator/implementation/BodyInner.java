/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.microsoft.azure.cognitiveservices.contentmoderator.BodyMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The BodyInner model.
 */
public class BodyInner {
    /**
     * Name of the list.
     */
    @JsonProperty(value = "Name")
    private String name;

    /**
     * Description of the list.
     */
    @JsonProperty(value = "Description")
    private String description;

    /**
     * Metadata of the list.
     */
    @JsonProperty(value = "Metadata")
    private BodyMetadata metadata;

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
     * @return the BodyInner object itself.
     */
    public BodyInner withName(String name) {
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
     * @return the BodyInner object itself.
     */
    public BodyInner withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public BodyMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the BodyInner object itself.
     */
    public BodyInner withMetadata(BodyMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
