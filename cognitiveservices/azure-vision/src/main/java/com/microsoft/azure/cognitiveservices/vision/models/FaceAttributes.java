/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Face Attributes.
 */
public class FaceAttributes {
    /**
     * Age in years.
     */
    @JsonProperty(value = "age")
    private Double age;

    /**
     * Gender: male or female. Possible values include: 'male', 'female'.
     */
    @JsonProperty(value = "gender")
    private String gender;

    /**
     * Smile intensity, a number between [0,1].
     */
    @JsonProperty(value = "smile")
    private Double smile;

    /**
     * Glasses type. Possible values are 'noGlasses', 'readingGlasses',
     * 'sunglasses', 'swimmingGoggles'. Possible values include: 'noGlasses',
     * 'readingGlasses', 'sunglasses', 'swimmingGoggles'.
     */
    @JsonProperty(value = "glasses")
    private String glasses;

    /**
     * The facialHair property.
     */
    @JsonProperty(value = "facialHair")
    private FacialHairProperties facialHair;

    /**
     * The headPose property.
     */
    @JsonProperty(value = "headPose")
    private HeadPoseProperties headPose;

    /**
     * The emotion property.
     */
    @JsonProperty(value = "emotion")
    private EmotionProperties emotion;

    /**
     * Get the age value.
     *
     * @return the age value
     */
    public Double age() {
        return this.age;
    }

    /**
     * Set the age value.
     *
     * @param age the age value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withAge(Double age) {
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
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withGender(String gender) {
        this.gender = gender;
        return this;
    }

    /**
     * Get the smile value.
     *
     * @return the smile value
     */
    public Double smile() {
        return this.smile;
    }

    /**
     * Set the smile value.
     *
     * @param smile the smile value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withSmile(Double smile) {
        this.smile = smile;
        return this;
    }

    /**
     * Get the glasses value.
     *
     * @return the glasses value
     */
    public String glasses() {
        return this.glasses;
    }

    /**
     * Set the glasses value.
     *
     * @param glasses the glasses value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withGlasses(String glasses) {
        this.glasses = glasses;
        return this;
    }

    /**
     * Get the facialHair value.
     *
     * @return the facialHair value
     */
    public FacialHairProperties facialHair() {
        return this.facialHair;
    }

    /**
     * Set the facialHair value.
     *
     * @param facialHair the facialHair value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withFacialHair(FacialHairProperties facialHair) {
        this.facialHair = facialHair;
        return this;
    }

    /**
     * Get the headPose value.
     *
     * @return the headPose value
     */
    public HeadPoseProperties headPose() {
        return this.headPose;
    }

    /**
     * Set the headPose value.
     *
     * @param headPose the headPose value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withHeadPose(HeadPoseProperties headPose) {
        this.headPose = headPose;
        return this;
    }

    /**
     * Get the emotion value.
     *
     * @return the emotion value
     */
    public EmotionProperties emotion() {
        return this.emotion;
    }

    /**
     * Set the emotion value.
     *
     * @param emotion the emotion value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withEmotion(EmotionProperties emotion) {
        this.emotion = emotion;
        return this;
    }

}
