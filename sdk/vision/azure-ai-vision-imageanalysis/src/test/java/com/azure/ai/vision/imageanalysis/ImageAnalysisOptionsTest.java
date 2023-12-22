// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;

class ImageAnalysisOptionsTest {

    @Test
    public void testDefaultOptions() {

        ImageAnalysisOptionsBuilder builder = new ImageAnalysisOptionsBuilder();
        ImageAnalysisOptions options = builder.build();

        assert options.getSmartCropsAspectRatios() == null;
        assert options.getLanguage() == null;
        assert options.getGenderNeutralCaption() == null;
        assert options.getModelVersion() == null;
    }

    @Test
    public void testAllOptionsAssignedAtOnce() {

        Boolean genderNeutralCaption = true;
        String language = "en";
        List<Double> aspectRatios = Arrays.asList(0.9, 1.33);
        String modelVersion = "latest";

        ImageAnalysisOptionsBuilder builder = new ImageAnalysisOptionsBuilder(
            language, genderNeutralCaption, aspectRatios, modelVersion);
        ImageAnalysisOptions options = builder.build();

        assert options.getSmartCropsAspectRatios() == aspectRatios;
        assert options.getLanguage() == language;
        assert options.getGenderNeutralCaption() == genderNeutralCaption;
        assert options.getModelVersion() == modelVersion;
    }

    @Test
    public void testAllOptionsUsingBuilderPattern() {

        Boolean genderNeutralCaption = true;
        String language = "en";
        List<Double> aspectRatios = Arrays.asList(0.9, 1.33);
        String modelVersion = "latest";

        ImageAnalysisOptions options = new ImageAnalysisOptionsBuilder()
            .setLanguage(language)
            .setGenderNeutralCaption(genderNeutralCaption)
            .setSmartCropsAspectRatios(aspectRatios)
            .setModelVersion(modelVersion)
            .build();

        assert options.getSmartCropsAspectRatios() == aspectRatios;
        assert options.getLanguage() == language;
        assert options.getGenderNeutralCaption() == genderNeutralCaption;
        assert options.getModelVersion() == modelVersion;
    }

    @Test
    public void testSomeOptionsUsingBuilderPattern() {

        Boolean genderNeutralCaption = true;
        String language = "en";

        ImageAnalysisOptions options = new ImageAnalysisOptionsBuilder()
            .setLanguage(language)
            .setGenderNeutralCaption(genderNeutralCaption)
            .build();

        assert options.getSmartCropsAspectRatios() == null;
        assert options.getLanguage() == language;
        assert options.getGenderNeutralCaption() == genderNeutralCaption;
        assert options.getModelVersion() == null;
    }
}
