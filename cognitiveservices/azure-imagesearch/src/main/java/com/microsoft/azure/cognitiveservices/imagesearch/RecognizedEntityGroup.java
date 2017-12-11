/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a group of previously recognized entities.
 */
public class RecognizedEntityGroup {
    /**
     * The regions of the image that contain entities.
     */
    @JsonProperty(value = "recognizedEntityRegions", required = true)
    private List<RecognizedEntityRegion> recognizedEntityRegions;

    /**
     * The name of the group where images of the entity were also found. The
     * following are possible groups. CelebRecognitionAnnotations: Similar to
     * CelebrityAnnotations but provides a higher probability of an accurate
     * match. CelebrityAnnotations: Contains celebrities such as actors,
     * politicians, athletes, and historical figures.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /**
     * Get the recognizedEntityRegions value.
     *
     * @return the recognizedEntityRegions value
     */
    public List<RecognizedEntityRegion> recognizedEntityRegions() {
        return this.recognizedEntityRegions;
    }

    /**
     * Set the recognizedEntityRegions value.
     *
     * @param recognizedEntityRegions the recognizedEntityRegions value to set
     * @return the RecognizedEntityGroup object itself.
     */
    public RecognizedEntityGroup withRecognizedEntityRegions(List<RecognizedEntityRegion> recognizedEntityRegions) {
        this.recognizedEntityRegions = recognizedEntityRegions;
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
     * @return the RecognizedEntityGroup object itself.
     */
    public RecognizedEntityGroup withName(String name) {
        this.name = name;
        return this;
    }

}
