// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImageAnalysisAsyncClientTest extends ImageAnalysisClientTestBase {

    private final Boolean sync = false; // All tests in this file use the async client

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeAsyncAllFeaturesFromFile() {

        createClient(true, true, sync, null);

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
        doAnalysis(methodName, sync, false, imageSource, visualFeatures, options, null);
    }

    @Test
    public void testAnalyzeAsyncSingleFeatureFromUrl() {

        createClient(true, true, sync, null);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageUrl;

        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.DENSE_CAPTIONS);
        ImageAnalysisOptions options = new ImageAnalysisOptions().setGenderNeutralCaption(true);
        doAnalysis(methodName + ":DenseCaptions", sync, false, imageSource, visualFeatures, options, null);

        visualFeatures =  Arrays.asList(VisualFeatures.SMART_CROPS);
        options = new ImageAnalysisOptions().setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33));
        doAnalysis(methodName + ":SmartCrops", sync, false, imageSource, visualFeatures, options, null);

        visualFeatures =  Arrays.asList(VisualFeatures.TAGS);
        options = new ImageAnalysisOptions().setLanguage("en");
        doAnalysis(methodName + ":Tags", sync, false, imageSource, visualFeatures, options, null);

        visualFeatures =  Arrays.asList(VisualFeatures.PEOPLE);
        doAnalysis(methodName + ":People", sync, false, imageSource, visualFeatures, null, null);
    }

    @Test
    public void testAnalyzeAsyncAllFeaturesFromUrlWithResponse() {

        createClient(true, true, sync, null);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageUrl;

        List<VisualFeatures> visualFeatures =  Arrays.asList(
                VisualFeatures.SMART_CROPS,
                VisualFeatures.CAPTION,
                VisualFeatures.DENSE_CAPTIONS,
                VisualFeatures.OBJECTS,
                VisualFeatures.PEOPLE,
                VisualFeatures.READ,
                VisualFeatures.TAGS);

        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en")
            .setGenderNeutralCaption(true)
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
            .setModelVersion("latest");

        doAnalysis(methodName, sync, true, imageSource, visualFeatures, options, null);
    }

    @Test
    public void testAnalyzeAsyncSingleFeatureFromFileWithResponse() {

        createClient(true, true, sync, null);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageFile;
        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.OBJECTS);
        RequestOptions requestOptions = new RequestOptions()
            .addHeader(HttpHeaderName.fromString("YourHeaderName"), "YourHeaderValue")
            .addQueryParam("key1", "value1")
            .addQueryParam("key2", "value2");

        doAnalysis(methodName, sync, true, imageSource, visualFeatures, null, requestOptions);
    }

    /***********************************************************************************
     *
     *                            ERROR TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeAsyncAuthenticationFailure() {

        createClient(true, false, sync, null);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageUrl;
        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.TAGS);
        ImageAnalysisOptions options = null;
        doAnalysisWithError(methodName, sync, imageSource, visualFeatures, options, 401, "Access denied");
    }
}
