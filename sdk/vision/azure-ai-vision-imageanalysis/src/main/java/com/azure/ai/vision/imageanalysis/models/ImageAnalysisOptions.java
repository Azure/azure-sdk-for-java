// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.vision.imageanalysis.models;

import java.util.List;

/**
 *  Options for a single Image Analysis operation.
 */
public final class ImageAnalysisOptions {

    String language = null;

    Boolean genderNeutralCaption = null;

    List<Double> smartCropsAspectRatios = null;

    String modelVersion = null;

    /**
     * Creates an instance of ImageAnalysisOptions class with default analysis options, as specified by the service.
     * The Language used will be "en" (for English).
     * Gender Neutral Caption is "false".
     * One Smart Crop region with an aspect ratio the service sees fit.
     * Model version is "latest".
     */
    public ImageAnalysisOptions() {
    }

    /**
     * Sets the desired language for output generation. At the moment this only applies to the 
     * visual feature {@link com.azure.ai.vision.imageanalysis.models.VisualFeatures#TAGS TAGS}.
     * If this parameter is not specified, the default value is "en" for English.
     * See <a href="https://aka.ms/cv-languages">https://aka.ms/cv-languages</a> for a list of 
     * supported languages and to which visual feature they apply.
     * 
     * @param language The langauge code to set, as specified in <a href="https://aka.ms/cv-languages">https://aka.ms/cv-languages</a>.
     * 
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of operations.
     */
    public ImageAnalysisOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the language code
     * 
     * @return The language code
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Sets the gender-neutral captioning option for visual 
     * features {@link com.azure.ai.vision.imageanalysis.models.VisualFeatures#CAPTION CAPTION} 
     * and {@link com.azure.ai.vision.imageanalysis.models.VisualFeatures#DENSE_CAPTIONS DENSE_CAPTIONS}.
     * If this parameter is not specified, the default value is "false".
     * By default captions may contain gender terms (for example: 'man', 'woman', or 'boy', 'girl').
     * If you set this to "true", those will be replaced with gender-neutral terms (for example: 'person' or 'child').
     * 
     * @param genderNeutralCaption Set to "true" to get gender neutral captions.
     * 
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of operations.
     */
    public ImageAnalysisOptions setGenderNeutralCaption(Boolean genderNeutralCaption) {
        this.genderNeutralCaption = genderNeutralCaption;
        return this;
    }

    /**
     * Get the gender neutral caption selection
     * 
     * @return The gender neutral caption selection
     */
    public Boolean isGenderNeutralCaption() {
        return this.genderNeutralCaption;
    }

    /**
     * Set a list of aspect ratios for the visual 
     * feature {@link com.azure.ai.vision.imageanalysis.models.VisualFeatures#SMART_CROPS SMART_CROPS}.
     * Aspect ratios are calculated by dividing the target crop width in pixels by the height in pixels.
     * Supported values are between 0.75 and 1.8 (inclusive).
     * If this parameter is not specified, the service will return one crop region with an aspect
     * ratio it sees fit between 0.5 and 2.0 (inclusive).
     * 
     * @param smartCropsAspectRatios The list of aspect ratios to set.
     * 
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of operations.
     */
    public ImageAnalysisOptions setSmartCropsAspectRatios(List<Double> smartCropsAspectRatios) {
        this.smartCropsAspectRatios = smartCropsAspectRatios;
        return this;
    }

    /**
     * Get the smart crop aspect rations
     * 
     * @return The smart crop aspect rations
     */
    public List<Double> getSmartCropsAspectRatios() {
        return this.smartCropsAspectRatios;
    }

    /**
     * Set the version of cloud AI-model used for analysis.
     * The format is the following: 'latest' (default value) or 'YYYY-MM-DD' or 'YYYY-MM-DD-preview', where 'YYYY',
     * 'MM', 'DD' are the year, month and day associated with the model.
     * This is not commonly set, as the default always gives the latest AI model with recent improvements.
     * If however you would like to make sure analysis results do not change over time, set this value to a specific
     * model version. Note that the model version is available in the Image Analysis Result (see com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult.getModelVersion).
     * 
     * @param modelVersion The model version to set.
     * 
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of operations.
     */
    public ImageAnalysisOptions setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Get the model version
     * 
     * @return The model version
     */
    public String getModelVersion() {
        return this.modelVersion;
    }
}
