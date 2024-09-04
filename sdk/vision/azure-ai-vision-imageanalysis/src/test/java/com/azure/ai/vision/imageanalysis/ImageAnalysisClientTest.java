// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.junit.jupiter.api.Test;

class ImageAnalysisClientTest extends ImageAnalysisClientTestBase {

    private final Boolean sync = true; // All tests in this file use the sync client

    /***********************************************************************************
     *
     *                            HAPPY PATH TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeSyncAllFeaturesFromUrl() {

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

        doAnalysis(methodName, sync, false, imageSource, visualFeatures, options, null);
    }

    @Test
    public void testAnalyzeSyncSingleFeatureFromFile() {

        List<Entry<String, String>> queryParams = new ArrayList<>();
        queryParams.add(new SimpleEntry<>("key1", "value1"));
        queryParams.add(new SimpleEntry<>("key2", "value2"));
        createClient(false, true, sync, queryParams);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageFile;
        ImageAnalysisOptions options = null;

        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.CAPTION);
        doAnalysis(methodName + ":Caption", sync, false, imageSource, visualFeatures, options, null);

        visualFeatures =  Arrays.asList(VisualFeatures.READ);
        doAnalysis(methodName + ":Read", sync, false, imageSource, visualFeatures, options, null);

        visualFeatures =  Arrays.asList(VisualFeatures.TAGS);
        doAnalysis(methodName + ":Tags", sync, false, imageSource, visualFeatures, options, null);
    }

    @Test
    public void testAnalyzeSyncAllFeaturesFromFileWithResponse() {

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

        ImageAnalysisOptions options = new ImageAnalysisOptions()
            .setLanguage("en")
            .setGenderNeutralCaption(true)
            .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
            .setModelVersion("latest");

        RequestOptions requestOptions = new RequestOptions()
            .addHeader(HttpHeaderName.fromString("YourHeaderName"), "YourHeaderValue")
            .addQueryParam("key1", "value1")
            .addQueryParam("key2", "value2");

        doAnalysis(methodName, sync, true, imageSource, visualFeatures, options, requestOptions);
    }

    @Test
    public void testAnalyzeSyncSingleFeatureFromUrlWithResponse() {

        createClient(false, true, sync, null);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = imageUrl;
        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.TAGS);

        doAnalysis(methodName, sync, true, imageSource, visualFeatures, null, null);
    }

    /***********************************************************************************
     *
     *                            ERROR TESTS
     *
     ***********************************************************************************/

    @Test
    public void testAnalyzeSyncImageUrlDoesNotExist() {

        createClient(true, true, sync, null);

        String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
        String imageSource = "https://www.this.is.a.bad.url.com/for/sure.jpg";
        List<VisualFeatures> visualFeatures =  Arrays.asList(VisualFeatures.CAPTION);
        ImageAnalysisOptions options = null;

        doAnalysisWithError(methodName, sync, imageSource, visualFeatures, options, 400, "image url is not accessible");
    }
}
