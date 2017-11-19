/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object describing a face identified in the image.
 */
public class FaceDescription {
    /**
     * Possible age of the face.
     */
    @JsonProperty(value = "age")
    private Integer age;

    /**
     * Possible gender of the face. Possible values include: 'Male', 'Female'.
     */
    @JsonProperty(value = "gender")
    private String gender;

    /**
     * The faceRectangle property.
     */
    @JsonProperty(value = "faceRectangle")
    private FaceRectangle faceRectangle;

    /**
     * Get the age value.
     *
     * @return the age value
     */
    public Integer age() {
        return this.age;
    }

    /**
     * Set the age value.
     *
     * @param age the age value to set
     * @return the FaceDescription object itself.
     */
    public FaceDescription withAge(Integer age) {
        this.age = age;
        return this;
    }

    /**
     * Get the gender value.
     *
     * @return the gender value
     */
    public String gender() {
        return this.gender;
    }

    /**
     * Set the gender value.
     *
     * @param gender the gender value to set
     * @return the FaceDescription object itself.
     */
    public FaceDescription withGender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * Get the faceRectangle value.
     *
     * @return the faceRectangle value
     */
    public FaceRectangle faceRectangle() {
        return this.faceRectangle;
    }

    /**
     * Set the faceRectangle value.
     *
     * @param faceRectangle the faceRectangle value to set
     * @return the FaceDescription object itself.
     */
    public FaceDescription withFaceRectangle(FaceRectangle faceRectangle) {
        this.faceRectangle = faceRectangle;
        return this;
    }

}
