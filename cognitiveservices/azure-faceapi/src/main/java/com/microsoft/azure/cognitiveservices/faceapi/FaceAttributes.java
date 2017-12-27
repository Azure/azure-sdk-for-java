/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi;

import java.util.List;
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
     * Possible gender of the face. Possible values include: 'male', 'female',
     * 'genderless'.
     */
    @JsonProperty(value = "gender")
    private Gender gender;

    /**
     * Smile intensity, a number between [0,1].
     */
    @JsonProperty(value = "smile")
    private Double smile;

    /**
     * The facialHair property.
     */
    @JsonProperty(value = "facialHair")
    private FacialHairProperties facialHair;

    /**
     * Glasses type if any of the face. Possible values include: 'noGlasses',
     * 'readingGlasses', 'sunglasses', 'swimmingGoggles'.
     */
    @JsonProperty(value = "glasses")
    private GlassesTypes glasses;

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
     * The hair property.
     */
    @JsonProperty(value = "hair")
    private HairProperties hair;

    /**
     * The makeup property.
     */
    @JsonProperty(value = "makeup")
    private MakeupProperties makeup;

    /**
     * The occlusion property.
     */
    @JsonProperty(value = "occlusion")
    private OcclusionProperties occlusion;

    /**
     * The accessories property.
     */
    @JsonProperty(value = "accessories")
    private List<AccessoryItem> accessories;

    /**
     * The blur property.
     */
    @JsonProperty(value = "blur")
    private BlurProperties blur;

    /**
     * The exposure property.
     */
    @JsonProperty(value = "exposure")
    private ExposureProperties exposure;

    /**
     * The noise property.
     */
    @JsonProperty(value = "noise")
    private NoiseProperties noise;

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
    public Gender gender() {
        return this.gender;
    }

    /**
     * Set the gender value.
     *
     * @param gender the gender value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withGender(Gender gender) {
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
     * Get the glasses value.
     *
     * @return the glasses value
     */
    public GlassesTypes glasses() {
        return this.glasses;
    }

    /**
     * Set the glasses value.
     *
     * @param glasses the glasses value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withGlasses(GlassesTypes glasses) {
        this.glasses = glasses;
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

    /**
     * Get the hair value.
     *
     * @return the hair value
     */
    public HairProperties hair() {
        return this.hair;
    }

    /**
     * Set the hair value.
     *
     * @param hair the hair value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withHair(HairProperties hair) {
        this.hair = hair;
        return this;
    }

    /**
     * Get the makeup value.
     *
     * @return the makeup value
     */
    public MakeupProperties makeup() {
        return this.makeup;
    }

    /**
     * Set the makeup value.
     *
     * @param makeup the makeup value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withMakeup(MakeupProperties makeup) {
        this.makeup = makeup;
        return this;
    }

    /**
     * Get the occlusion value.
     *
     * @return the occlusion value
     */
    public OcclusionProperties occlusion() {
        return this.occlusion;
    }

    /**
     * Set the occlusion value.
     *
     * @param occlusion the occlusion value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withOcclusion(OcclusionProperties occlusion) {
        this.occlusion = occlusion;
        return this;
    }

    /**
     * Get the accessories value.
     *
     * @return the accessories value
     */
    public List<AccessoryItem> accessories() {
        return this.accessories;
    }

    /**
     * Set the accessories value.
     *
     * @param accessories the accessories value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withAccessories(List<AccessoryItem> accessories) {
        this.accessories = accessories;
        return this;
    }

    /**
     * Get the blur value.
     *
     * @return the blur value
     */
    public BlurProperties blur() {
        return this.blur;
    }

    /**
     * Set the blur value.
     *
     * @param blur the blur value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withBlur(BlurProperties blur) {
        this.blur = blur;
        return this;
    }

    /**
     * Get the exposure value.
     *
     * @return the exposure value
     */
    public ExposureProperties exposure() {
        return this.exposure;
    }

    /**
     * Set the exposure value.
     *
     * @param exposure the exposure value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withExposure(ExposureProperties exposure) {
        this.exposure = exposure;
        return this;
    }

    /**
     * Get the noise value.
     *
     * @return the noise value
     */
    public NoiseProperties noise() {
        return this.noise;
    }

    /**
     * Set the noise value.
     *
     * @param noise the noise value to set
     * @return the FaceAttributes object itself.
     */
    public FaceAttributes withNoise(NoiseProperties noise) {
        this.noise = noise;
        return this;
    }

}
