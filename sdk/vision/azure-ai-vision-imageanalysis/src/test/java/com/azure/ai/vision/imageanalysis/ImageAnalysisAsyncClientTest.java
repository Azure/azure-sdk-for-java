// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import java.util.Arrays;
import java.util.List;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;

class ImageAnalysisAsyncClientTest extends ImageAnalysisClientTestBase {

    private final Boolean sync = false; // All tests in this file use the async client

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeAsyncAllFeaturesFromFile() throws MalformedURLException {

        createClientForStandardAnalysis(sync);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageFile;
        List<VisualFeatures> visualFeatures =  Arrays.asList(
                VisualFeatures.SMART_CROPS,
                VisualFeatures.CAPTION,
                VisualFeatures.DENSE_CAPTIONS,
                VisualFeatures.OBJECTS,
                VisualFeatures.PEOPLE,
                VisualFeatures.READ,
                VisualFeatures.TAGS);
        ImageAnalysisOptions options = null;
        doAnalysis(methodName, sync, imageSource, visualFeatures, options);
    }

    @Test
    public void testAnalyzeAsyncSingleFeatureFromUrl() throws MalformedURLException {

        createClientForStandardAnalysis(sync);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageUrl;

        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.DENSE_CAPTIONS);
        ImageAnalysisOptions options = new ImageAnalysisOptions().setGenderNeutralCaption(true);
        doAnalysis(methodName + ":DenseCaptions", sync, imageSource, visualFeatures, options);

        visualFeatures =  Arrays.asList(VisualFeatures.SMART_CROPS);
        options = new ImageAnalysisOptions().setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33));
        doAnalysis(methodName + ":SmartCrops", sync, imageSource, visualFeatures, options);

        visualFeatures =  Arrays.asList(VisualFeatures.TAGS);
        options = new ImageAnalysisOptions().setLanguage("en");
        doAnalysis(methodName + ":Tags", sync, imageSource, visualFeatures, options);

        visualFeatures =  Arrays.asList(VisualFeatures.PEOPLE);
        doAnalysis(methodName + ":People", sync, imageSource, visualFeatures, null);
    }

    /***********************************************************************************
     *
     *                            ERROR TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeAsyncAuthenticationFailure() throws MalformedURLException {

        createClientForAuthenticationFailure(sync);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageUrl;
        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.TAGS);
        ImageAnalysisOptions options = null;
        doAnalysisWithError(methodName, sync, imageSource, visualFeatures, options, 401, "Access denied");
    }
}
