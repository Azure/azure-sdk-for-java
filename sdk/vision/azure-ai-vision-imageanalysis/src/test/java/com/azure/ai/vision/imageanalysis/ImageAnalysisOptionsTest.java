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

        ImageAnalysisOptions options = new ImageAnalysisOptions();

        assert options.getSmartCropsAspectRatios() == null;
        assert options.getLanguage() == null;
        assert options.isGenderNeutralCaption() == null;
        assert options.getModelVersion() == null;
    }

    @Test
    public void testAllOptionsUsingBuilderPattern() {

        Boolean genderNeutralCaption = true;
        String language = "de";
        List<Double> aspectRatios = Arrays.asList(0.9, 1.33);
        String modelVersion = "latest";

        ImageAnalysisOptions options = new ImageAnalysisOptions().setLanguage(language)
            .setGenderNeutralCaption(genderNeutralCaption)
            .setSmartCropsAspectRatios(aspectRatios)
            .setModelVersion(modelVersion);

        assert options.getSmartCropsAspectRatios() == aspectRatios;
        assert options.getLanguage() == language;
        assert options.isGenderNeutralCaption() == genderNeutralCaption;
        assert options.getModelVersion() == modelVersion;
    }

    @Test
    public void testSomeOptionsUsingBuilderPattern() {

        Boolean genderNeutralCaption = true;
        String language = "de";

        ImageAnalysisOptions options
            = new ImageAnalysisOptions().setLanguage(language).setGenderNeutralCaption(genderNeutralCaption);

        assert options.getSmartCropsAspectRatios() == null;
        assert options.getLanguage() == language;
        assert options.isGenderNeutralCaption() == genderNeutralCaption;
        assert options.getModelVersion() == null;
    }
}
