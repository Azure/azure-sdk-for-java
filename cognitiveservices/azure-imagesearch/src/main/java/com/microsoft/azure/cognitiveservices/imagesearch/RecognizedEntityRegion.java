/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a region of the image where an entity was found and a list of
 * entities that might match it.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = RecognizedEntityRegion.class)
@JsonTypeName("RecognizedEntityRegion")
public class RecognizedEntityRegion extends Response {
    /**
     * A region of the image that contains an entity. The values of the
     * rectangle are relative to the width and height of the original image and
     * are in the range 0.0 through 1.0. For example, if the image is 300x200
     * and the region's top, left corner is at point (10, 20) and the bottom,
     * right corner is at point (290, 150), then the normalized rectangle is:
     * Left = 0.0333333333333333, Top = 0.1, Right = 0.9666666666666667, Bottom
     * = 0.75. For people, the region represents the person's face.
     */
    @JsonProperty(value = "region", access = JsonProperty.Access.WRITE_ONLY)
    private NormalizedRectangle region;

    /**
     * A list of entities that Bing believes match the entity found in the
     * region. The entities are in descending order of confidence (see the
     * matchConfidence field of RecognizedEntity).
     */
    @JsonProperty(value = "matchingEntities", access = JsonProperty.Access.WRITE_ONLY)
    private List<RecognizedEntity> matchingEntities;

    /**
     * Get the region value.
     *
     * @return the region value
     */
    public NormalizedRectangle region() {
        return this.region;
    }

    /**
     * Get the matchingEntities value.
     *
     * @return the matchingEntities value
     */
    public List<RecognizedEntity> matchingEntities() {
        return this.matchingEntities;
    }

}
