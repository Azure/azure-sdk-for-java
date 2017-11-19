/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.computervision.OcrRegion;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The OcrResultInner model.
 */
public class OcrResultInner {
    /**
     * The language property.
     */
    @JsonProperty(value = "language")
    private OcrResultInner language;

    /**
     * The angle, in degrees, of the detected text with respect to the closest
     * horizontal or vertical direction. After rotating the input image
     * clockwise by this angle, the recognized text lines become horizontal or
     * vertical. In combination with the orientation property it can be used to
     * overlay recognition results correctly on the original image, by rotating
     * either the original image or recognition results by a suitable angle
     * around the center of the original image. If the angle cannot be
     * confidently detected, this property is not present. If the image
     * contains text at different angles, only part of the text will be
     * recognized correctly.
     */
    @JsonProperty(value = "textAngle")
    private Double textAngle;

    /**
     * Orientation of the text recognized in the image. The value
     * (up,down,left, or right) refers to the direction that the top of the
     * recognized text is facing, after the image has been rotated around its
     * center according to the detected text angle (see textAngle property).
     */
    @JsonProperty(value = "orientation")
    private String orientation;

    /**
     * An array of objects, where each object represents a region of recognized
     * text.
     */
    @JsonProperty(value = "regions")
    private List<OcrRegion> regions;

    /**
     * Get the language value.
     *
     * @return the language value
     */
    public OcrResultInner language() {
        return this.language;
    }

    /**
     * Set the language value.
     *
     * @param language the language value to set
     * @return the OcrResultInner object itself.
     */
    public OcrResultInner withLanguage(OcrResultInner language) {
        this.language = language;
        return this;
    }

    /**
     * Get the textAngle value.
     *
     * @return the textAngle value
     */
    public Double textAngle() {
        return this.textAngle;
    }

    /**
     * Set the textAngle value.
     *
     * @param textAngle the textAngle value to set
     * @return the OcrResultInner object itself.
     */
    public OcrResultInner withTextAngle(Double textAngle) {
        this.textAngle = textAngle;
        return this;
    }

    /**
     * Get the orientation value.
     *
     * @return the orientation value
     */
    public String orientation() {
        return this.orientation;
    }

    /**
     * Set the orientation value.
     *
     * @param orientation the orientation value to set
     * @return the OcrResultInner object itself.
     */
    public OcrResultInner withOrientation(String orientation) {
        this.orientation = orientation;
        return this;
    }

    /**
     * Get the regions value.
     *
     * @return the regions value
     */
    public List<OcrRegion> regions() {
        return this.regions;
    }

    /**
     * Set the regions value.
     *
     * @param regions the regions value to set
     * @return the OcrResultInner object itself.
     */
    public OcrResultInner withRegions(List<OcrRegion> regions) {
        this.regions = regions;
        return this;
    }

}
