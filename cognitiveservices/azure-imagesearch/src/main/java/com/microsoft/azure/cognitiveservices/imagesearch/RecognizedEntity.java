/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a recognized entity.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = RecognizedEntity.class)
@JsonTypeName("RecognizedEntity")
public class RecognizedEntity extends Response {
    /**
     * The entity that was recognized. The following are the possible entity
     * objects: Person.
     */
    @JsonProperty(value = "entity", access = JsonProperty.Access.WRITE_ONLY)
    private Thing entity;

    /**
     * The confidence that Bing has that the entity in the image matches this
     * entity. The confidence ranges from 0.0 through 1.0 with 1.0 being very
     * confident.
     */
    @JsonProperty(value = "matchConfidence", access = JsonProperty.Access.WRITE_ONLY)
    private Double matchConfidence;

    /**
     * Get the entity value.
     *
     * @return the entity value
     */
    public Thing entity() {
        return this.entity;
    }

    /**
     * Get the matchConfidence value.
     *
     * @return the matchConfidence value
     */
    public Double matchConfidence() {
        return this.matchConfidence;
    }

}
