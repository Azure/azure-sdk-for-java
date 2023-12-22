// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.vision.imageanalysis;

import java.util.List;

/**
 *  Options for a single Image Analysis operation.
 *  An object of this class can only be constructed through the
 *  {@link ImageAnalysisOptionsBuilder#build() build} method of the {@link ImageAnalysisOptionsBuilder} class.
 */
public final class ImageAnalysisOptions {

    private final String language;

    private final Boolean genderNeutralCaption;

    private final List<Double> smartCropsAspectRatios;

    private final String modelVersion;

    ImageAnalysisOptions(ImageAnalysisOptionsBuilder builder) {
        this.language = builder.language;
        this.genderNeutralCaption = builder.genderNeutralCaption;
        this.smartCropsAspectRatios = builder.smartCropsAspectRatios;
        this.modelVersion = builder.modelVersion;
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
     * Get the gender neutral caption selection
     * 
     * @return The gender neutral caption selection
     */
    public Boolean getGenderNeutralCaption() {
        return this.genderNeutralCaption;
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
     * Get the model version
     * 
     * @return The model version
     */
    public String getModelVersion() {
        return this.modelVersion;
    }
}
