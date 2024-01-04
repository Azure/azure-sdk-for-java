// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;
import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;

class ImageAnalysisClientTest extends ImageAnalysisClientTestBase {

    private final Boolean sync = true; // All tests in this file use the sync client

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeSyncAllFeaturesFromUrl() throws MalformedURLException {

        createClientForStandardAnalysis(sync);

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
        Boolean genderNeutralCaption = true;
        String language = "en";
        List<Double> aspectRatios = Arrays.asList(0.9, 1.33);
        String modelVersion = "latest";
        ImageAnalysisOptions options = new ImageAnalysisOptions(
            language, genderNeutralCaption, aspectRatios, modelVersion);
        doAnalysis(methodName, sync, imageSource, visualFeatures, options);
    }

    @Test
    public void testAnalyzeSyncSingleFeatureFromFile() throws MalformedURLException {

        // Note: The test is missing automatic validation that the connection URL includes "&key1=value1&key2=value2". Need
        // to do this manually.
        List<Entry<String, String>> queryParams = new ArrayList<>();
        queryParams.add(new SimpleEntry<>("key1", "value1"));
        queryParams.add(new SimpleEntry<>("key2", "value2"));
        createClientForStandardAnalysis(sync, queryParams);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageFile;
        ImageAnalysisOptions options = null;

        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.CAPTION);
        doAnalysis(methodName + ":Caption", sync, imageSource, visualFeatures, options);

        visualFeatures =  Arrays.asList(VisualFeatures.READ);
        doAnalysis(methodName + ":Read", sync, imageSource, visualFeatures, options);

        visualFeatures =  Arrays.asList(VisualFeatures.TAGS);
        doAnalysis(methodName + ":Tags", sync, imageSource, visualFeatures, options);
    }

    /***********************************************************************************
     *
     *                            ERROR TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeSyncImageUrlDoesNotExist() throws MalformedURLException {

        createClientForStandardAnalysis(sync);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = "https://www.this.is.a.bad.url.com/for/sure.jpg";
        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.CAPTION);
        ImageAnalysisOptions options = null;
        doAnalysisWithError(methodName, sync, imageSource, visualFeatures, options, 400, "image url is not accessible");
    }
}
