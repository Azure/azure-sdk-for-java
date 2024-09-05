// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import com.azure.identity.DefaultAzureCredentialBuilder;

// See https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


class ImageAnalysisClientTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(ImageAnalysisClientTestBase.class);

    final Boolean printResults = false; // Set to true to print results to console window

    // We use a single image (the same one) for all error-free tests, one hosted on the web and one local
    final String imageUrl = "https://aka.ms/azsdk/image-analysis/sample.jpg";
    final String imageFile = "./src/test/java/com/azure/ai/vision/imageanalysis/sample.jpg";

    // The client that will be used for tests
    private ImageAnalysisClient client = null;
    private ImageAnalysisAsyncClient asyncClient = null;

    protected void createClient(
        Boolean useKeyAuth,
        Boolean useRealKey,
        Boolean sync,
        List<Entry<String, String>> queryParams) {

        TestMode testMode = getTestMode();

        // Define endpoint and auth credentials
        String endpoint = "https://fake-resource-name.cognitiveservices.azure.com";
        String key = "00000000000000000000000000000000";

        if (testMode == TestMode.LIVE || testMode == TestMode.RECORD) {
            endpoint = Configuration.getGlobalConfiguration().get("VISION_ENDPOINT");
            assertTrue(endpoint != null && !endpoint.isEmpty(), "Endpoint URL is required to run live tests.");

            if (useKeyAuth && useRealKey) {
                key = Configuration.getGlobalConfiguration().get("VISION_KEY"); 
                assertTrue(endpoint != null && !endpoint.isEmpty(), "API key is required to run live tests.");
            }
        }

        // Create the client builder
        ImageAnalysisClientBuilder imageAnalysisClientBuilder =
                new ImageAnalysisClientBuilder()
                        .endpoint(endpoint)
                        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Update the client builder with optional custom query parameters
        if (queryParams != null) {
            for (Entry<String, String> queryParam : queryParams) {
                imageAnalysisClientBuilder.addPolicy(new ImageAnalysisAddQueryParamPolicy(queryParam.getKey(), queryParam.getValue()));
            }
        }

        // Updat the client building with credentials and recording/playback policies
        if (getTestMode() == TestMode.LIVE) {
            if (useKeyAuth) {
                imageAnalysisClientBuilder.credential(new KeyCredential(key));
            } else {
                imageAnalysisClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
            }
        } else if (getTestMode() == TestMode.RECORD) {
            imageAnalysisClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
            if (useKeyAuth) {
                imageAnalysisClientBuilder.credential(new KeyCredential(key));
            } else {
                imageAnalysisClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
            }
        } else if (getTestMode() == TestMode.PLAYBACK) {
            imageAnalysisClientBuilder.httpClient(interceptorManager.getPlaybackClient());
            if (useKeyAuth) {
                imageAnalysisClientBuilder.credential(new KeyCredential(key));
            } else {
                imageAnalysisClientBuilder.credential(new MockTokenCredential());
            }
        }

        // Set recording filters
        if (!interceptorManager.isLiveMode()) {
            // Remove `operation-location`, `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK2003", "AZSDK2030", "AZSDK3430", "AZSDK3493");
        }

        if (sync) {
            client = imageAnalysisClientBuilder.buildClient();
        } else {
            asyncClient = imageAnalysisClientBuilder.buildAsyncClient();
        }
    }

    protected void doAnalysis(
        String testName, // Any label the uniquely defines the test. Used in console printout.
        Boolean sync, // 'true' to use synchronous client. 'false' to use asynchronous client.
        Boolean analyzeWithResponse, // 'true' to use analze()/analyzeFromUrl(). 'false' to use analyzeWithResponse()/analyzeFromUrlWithResponse().
        String imageSource, // Image URL or image file path
        List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions imageAnalysisOptions, // can be null
        RequestOptions requestOptions) { // can be null

        boolean fromUrl = imageSource.startsWith("http");
        Boolean genderNeutralCaption = null;
        List<Double> aspectRatios = null;

        if (imageAnalysisOptions != null) {
            genderNeutralCaption = imageAnalysisOptions.isGenderNeutralCaption();
            aspectRatios = imageAnalysisOptions.getSmartCropsAspectRatios();
        }

        if (sync) {
            ImageAnalysisResult result = null;
            if (fromUrl) {
                if (!analyzeWithResponse) {
                    result = client.analyzeFromUrl(
                        imageSource,
                        visualFeatures,
                        imageAnalysisOptions);
                } else {
                    Response<ImageAnalysisResult> response = client.analyzeFromUrlWithResponse(
                        imageSource,
                        visualFeatures,
                        imageAnalysisOptions,
                        requestOptions);
                    printHttpRequestAndResponse(response);
                    result = response.getValue();
                }
            } else {
                if (!analyzeWithResponse) {
                    result = client.analyze(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        imageAnalysisOptions);
                } else {
                    Response<ImageAnalysisResult> response = client.analyzeWithResponse(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        imageAnalysisOptions,
                        requestOptions);
                    printHttpRequestAndResponse(response);
                    result = response.getValue();
                }
            }

            // Optional: console printout of all results
            if (printResults) {
                printAnalysisResults(testName, result);
            }

            // Validate all results
            validateAnalysisResult(result, visualFeatures, genderNeutralCaption, aspectRatios);

        } else { // sync = false
            ImageAnalysisResult result = null;
            if (fromUrl) {
                if (!analyzeWithResponse) {
                    result = asyncClient.analyzeFromUrl(
                        imageSource,
                        visualFeatures,
                        imageAnalysisOptions).block();
                } else {
                    Response<ImageAnalysisResult> response = asyncClient.analyzeFromUrlWithResponse(
                        imageSource,
                        visualFeatures,
                        imageAnalysisOptions,
                        requestOptions).block();
                    printHttpRequestAndResponse(response);
                    result = response.getValue();
                }
            } else {
                if (!analyzeWithResponse) {
                    result = asyncClient.analyze(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        imageAnalysisOptions).block();
                } else {
                    Response<ImageAnalysisResult> response = asyncClient.analyzeWithResponse(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        imageAnalysisOptions,
                        requestOptions).block();
                    printHttpRequestAndResponse(response);
                    result = response.getValue();
                }
            }

            // Optional: console printout of all results
            if (printResults) {
                printAnalysisResults(testName, result);
            }

            // Validate all results
            validateAnalysisResult(result, visualFeatures, genderNeutralCaption, aspectRatios);
        }
    }

    protected void doAnalysisWithError(
        String testName,
        Boolean sync,
        String imageSource,
        List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options,
        int expectedStatusCode,
        String expectedMessageContains) {

        boolean fromUrl = imageSource.startsWith("http");
        ImageAnalysisResult result = null;

        if (sync) {
            try {
                if (fromUrl) {
                    result = client.analyzeFromUrl(
                        imageSource,
                        visualFeatures,
                        options);
                } else {
                    result = client.analyze(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        options);
                }
            } catch (HttpResponseException e) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Expected exception: " + e.getMessage());
                assertEquals(expectedStatusCode, e.getResponse().getStatusCode());
                assertTrue(e.getMessage().contains(expectedMessageContains));
                return;
            }
        } else {
            try {
                if (fromUrl) {
                    result = asyncClient.analyzeFromUrl(
                        imageSource,
                        visualFeatures,
                        options).block();
                } else {
                    result = asyncClient.analyze(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        options).block();
                }
            } catch (HttpResponseException e) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Expected exception: " + e.getMessage());
                assertEquals(expectedStatusCode, e.getResponse().getStatusCode());
                assertTrue(e.getMessage().contains(expectedMessageContains));
                return;
            }
        }

        LOGGER.log(LogLevel.VERBOSE, () -> "Test should have thrown an exception, but it did not");
        fail();
    }

    private static void validateAnalysisResult(
        ImageAnalysisResult result,
        List<VisualFeatures> expectedFeatures,
        Boolean genderNeutralCaption,
        List<Double> aspectRatios) {

        validateMetadata(result);
        validateModelVersion(result);

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.CAPTION)) {
            validateCaption(result, genderNeutralCaption);
        } else {
            assertNull(result.getCaption());
        }

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.DENSE_CAPTIONS)) {
            validateDenseCaptions(result);
        } else {
            assertNull(result.getDenseCaptions());
        }

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.OBJECTS)) {
            validateObjects(result);
        } else {
            assertNull(result.getObjects());
        }

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.TAGS)) {
            validateTags(result);
        } else {
            assertNull(result.getTags());
        }

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.PEOPLE)) {
            validatePeople(result);
        } else {
            assertNull(result.getPeople());
        }

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.SMART_CROPS)) {
            validateSmartCrops(result, aspectRatios);
        } else {
            assertNull(result.getSmartCrops());
        }

        if (expectedFeatures != null && expectedFeatures.contains(VisualFeatures.READ)) {
            validateRead(result);
        } else {
            assertNull(result.getRead());
        }
    }

    private static void validateMetadata(ImageAnalysisResult result) {
        assertNotNull(result.getMetadata());
        assertEquals(576, result.getMetadata().getHeight());
        assertEquals(864, result.getMetadata().getWidth());
    }

    private static void validateModelVersion(ImageAnalysisResult result) {
        assertNotNull(result.getModelVersion());
        assertEquals("2023-10-01", result.getModelVersion());
    }

    private static void validateCaption(ImageAnalysisResult result, Boolean genderNeutralCaption) {
        assertNotNull(result.getCaption());
        assertNotNull(result.getCaption().getText());
        if (genderNeutralCaption != null && genderNeutralCaption) {
            assertTrue(result.getCaption().getText().toLowerCase().contains("person"));
        } else {
            assertTrue(result.getCaption().getText().toLowerCase().contains("woman"));
        }
        assertTrue(result.getCaption().getText().toLowerCase().contains("table"));
        assertTrue(result.getCaption().getText().toLowerCase().contains("laptop"));
        assertTrue(result.getCaption().getConfidence() > 0.0);
        assertTrue(result.getCaption().getConfidence() < 1.0);
    }

    private static void validateDenseCaptions(ImageAnalysisResult result) {
        assertNotNull(result.getDenseCaptions());
        assertTrue(result.getDenseCaptions().getValues().size() > 1);

        // First dense caption should apply to the whole image, and be identical to the caption found in CaptionResult
        DenseCaption firstDenseCaption = result.getDenseCaptions().getValues().get(0);
        assertNotNull(firstDenseCaption);
        assertNotNull(firstDenseCaption.getText());
        if (result.getCaption() != null) {
            assertEquals(firstDenseCaption.getText(), result.getCaption().getText());
        } else {
            assertFalse(firstDenseCaption.getText().isEmpty());
        }
        assertEquals(0, firstDenseCaption.getBoundingBox().getX());
        assertEquals(0, firstDenseCaption.getBoundingBox().getY());
        assertEquals(firstDenseCaption.getBoundingBox().getHeight(), result.getMetadata().getHeight());
        assertEquals(firstDenseCaption.getBoundingBox().getWidth(), result.getMetadata().getWidth());

        // Sanity checks on all dense captions
        for (DenseCaption denseCaption : result.getDenseCaptions().getValues()) {
            assertNotNull(denseCaption);
            assertNotNull(denseCaption.getText());
            assertFalse(denseCaption.getText().isEmpty());
            assertTrue(denseCaption.getConfidence() > 0.0);
            assertTrue(denseCaption.getConfidence() < 1.0);
            assertTrue(denseCaption.getBoundingBox().getX() >= 0);
            assertTrue(denseCaption.getBoundingBox().getY() >= 0);
            assertTrue(denseCaption.getBoundingBox().getHeight() <= result.getMetadata().getHeight() - denseCaption.getBoundingBox().getY());
            assertTrue(denseCaption.getBoundingBox().getWidth() <= result.getMetadata().getWidth() - denseCaption.getBoundingBox().getX());
        }

        // Make sure each dense caption is unique
        for (int i = 0; i < result.getDenseCaptions().getValues().size(); i++) {
            DenseCaption denseCaption = result.getDenseCaptions().getValues().get(i);
            for (int j = i + 1; j < result.getDenseCaptions().getValues().size(); j++) {
                DenseCaption otherDenseCaption = result.getDenseCaptions().getValues().get(j);
                // Do not include the check below. It's okay to have two identical dense captions since they have different bounding boxes.
                // assertFalse(otherDenseCaption.getText().equals(denseCaption.getText()));
                assertFalse(
                    otherDenseCaption.getBoundingBox().getX() == denseCaption.getBoundingBox().getX()
                    && otherDenseCaption.getBoundingBox().getY() == denseCaption.getBoundingBox().getY()
                    && otherDenseCaption.getBoundingBox().getHeight() == denseCaption.getBoundingBox().getHeight()
                    && otherDenseCaption.getBoundingBox().getWidth() == denseCaption.getBoundingBox().getWidth());
            }
        }
    }

    private static void validateObjects(ImageAnalysisResult result) {
        ObjectsResult objectsResult = result.getObjects();
        validateObjects(objectsResult);
    }

    private static void validateObjects(ObjectsResult objectsResult) {
        assertNotNull(objectsResult);
        assertTrue(objectsResult.getValues().size() > 1);

        boolean found1 = false;
        for (DetectedObject object : objectsResult.getValues()) {
            assertNotNull(object);
            assertNotNull(object.getTags());
            assertEquals(1, object.getTags().size());
            DetectedTag tag = object.getTags().get(0);
            assertNotNull(tag);
            assertNotNull(tag.getName());
            assertFalse(tag.getName().isEmpty());
            assertTrue(tag.getConfidence() > 0.0);
            assertTrue(tag.getConfidence() < 1.0);
            // We expect to see this in the list of objects
            if (tag.getName().equalsIgnoreCase("person")) {
                found1 = true;
            }
        }
        assertTrue(found1);

        // Make sure each object box is unique
        for (int i = 0; i < objectsResult.getValues().size(); i++) {
            for (int j = i + 1; j < objectsResult.getValues().size(); j++) {
                ImageBoundingBox boxI = objectsResult.getValues().get(i).getBoundingBox();
                ImageBoundingBox boxJ = objectsResult.getValues().get(j).getBoundingBox();
                assertFalse(
                    boxI.getX() == boxJ.getX()
                    && boxI.getY() == boxJ.getY()
                    && boxI.getHeight() == boxJ.getHeight()
                    && boxI.getWidth() == boxJ.getWidth());
            }
        }
    }

    private static void validateTags(ImageAnalysisResult result) {
        TagsResult tags = result.getTags();
        validateTags(tags);
    }

    private static void validateTags(TagsResult tagsResult) {
        assertNotNull(tagsResult);
        assertNotNull(tagsResult.getValues());
        assertTrue(tagsResult.getValues().size() > 1);

        boolean found1 = false, found2 = false;
        for (DetectedTag tag : tagsResult.getValues()) {
            assertNotNull(tag.getName());
            assertFalse(tag.getName().isEmpty());
            assertTrue(tag.getConfidence() > 0.0);
            assertTrue(tag.getConfidence() < 1.0);
            // We expect to see both of these in the list of tags
            if (tag.getName().equalsIgnoreCase("person")) {
                found1 = true;
            }
            if (tag.getName().equalsIgnoreCase("laptop")) {
                found2 = true;
            }
        }
        assertTrue(found1);
        assertTrue(found2);

        // Make sure each tag is unique
        for (int i = 0; i < tagsResult.getValues().size(); i++) {
            for (int j = i + 1; j < tagsResult.getValues().size(); j++) {
                assertNotEquals(tagsResult.getValues().get(i).getName(), tagsResult.getValues().get(j).getName());
            }
        }
    }

    private static void validatePeople(ImageAnalysisResult result) {
        assertNotNull(result.getPeople());
        assertFalse(result.getPeople().getValues().isEmpty());

        for (DetectedPerson person : result.getPeople().getValues()) {
            assertTrue(person.getConfidence() > 0.0);
            assertTrue(person.getConfidence() < 1.0);
            assertTrue(person.getBoundingBox().getX() >= 0);
            assertTrue(person.getBoundingBox().getY() >= 0);
            assertTrue(person.getBoundingBox().getHeight() <= result.getMetadata().getHeight() - person.getBoundingBox().getY());
            assertTrue(person.getBoundingBox().getWidth() <= result.getMetadata().getWidth() - person.getBoundingBox().getX());
        }

        // Make sure each person is unique
        for (int i = 0; i < result.getPeople().getValues().size(); i++) {
            DetectedPerson person = result.getPeople().getValues().get(i);
            for (int j = i + 1; j < result.getPeople().getValues().size(); j++) {
                DetectedPerson otherPerson = result.getPeople().getValues().get(j);
                assertFalse(
                    otherPerson.getBoundingBox().getX() == person.getBoundingBox().getX()
                    && otherPerson.getBoundingBox().getY() == person.getBoundingBox().getY()
                    && otherPerson.getBoundingBox().getHeight() == person.getBoundingBox().getHeight()
                    && otherPerson.getBoundingBox().getWidth() == person.getBoundingBox().getWidth());
            }
        }
    }

    private static void validateSmartCrops(ImageAnalysisResult result, List<Double> aspectRatios) {
        assertNotNull(result.getSmartCrops());
        List<CropRegion> listCropRegions = result.getSmartCrops().getValues();
        if (aspectRatios == null) {
            assertEquals(1, listCropRegions.size());
            assertTrue(listCropRegions.get(0).getAspectRatio() >= 0.5);
            assertTrue(listCropRegions.get(0).getAspectRatio() <= 2.0);
        } else {
            assertEquals(listCropRegions.size(), aspectRatios.size());
            for (int i = 0; i < listCropRegions.size(); i++) {
                assertEquals(listCropRegions.get(i).getAspectRatio(), aspectRatios.get(i));
                assertTrue(listCropRegions.get(0).getAspectRatio() >= 0.75);
                assertTrue(listCropRegions.get(0).getAspectRatio() <= 1.8);
            }
        }

        for (CropRegion region : listCropRegions) {
            assertTrue(region.getBoundingBox().getX() >= 0);
            assertTrue(region.getBoundingBox().getY() >= 0);
            assertTrue(region.getBoundingBox().getHeight() <= result.getMetadata().getHeight() - region.getBoundingBox().getY());
            assertTrue(region.getBoundingBox().getWidth() <= result.getMetadata().getWidth() - region.getBoundingBox().getX());
        }

        // Make sure each bounding box is unique
        for (int i = 0; i < listCropRegions.size(); i++) {
            CropRegion region = listCropRegions.get(i);
            for (int j = i + 1; j < listCropRegions.size(); j++) {
                CropRegion otherRegion = listCropRegions.get(j);
                assertFalse(
                    otherRegion.getBoundingBox().getX() == region.getBoundingBox().getX()
                    && otherRegion.getBoundingBox().getY() == region.getBoundingBox().getY()
                    && otherRegion.getBoundingBox().getHeight() == region.getBoundingBox().getHeight()
                    && otherRegion.getBoundingBox().getWidth() == region.getBoundingBox().getWidth());
            }
        }
    }

    private static void validateRead(ImageAnalysisResult result) {

        ReadResult readResult = result.getRead();
        assertNotNull(readResult);
        assertNotNull(readResult.getBlocks());
        assertEquals(1, readResult.getBlocks().size());

        DetectedTextBlock block = readResult.getBlocks().get(0);
        assertNotNull(block);

        List<DetectedTextLine> lines = block.getLines();
        assertNotNull(lines);
        assertEquals(3, lines.size());

        // Do some validations on the first line
        DetectedTextLine line = lines.get(0);
        assertNotNull(line);
        assertEquals("Sample text", line.getText());

        List<ImagePoint> polygon = line.getBoundingPolygon();
        assertNotNull(polygon);
        assertEquals(4, polygon.size());
        for (ImagePoint imagePoint : polygon) {
            assertTrue(imagePoint.getX() > 0);
            assertTrue(imagePoint.getY() > 0);
        }

        // Do some verifications on the 3rd line
        line = lines.get(2);
        assertNotNull(line);
        assertEquals("123 456", line.getText());

        List<DetectedTextWord> words = line.getWords();
        assertNotNull(words);
        assertEquals(2, words.size());

        DetectedTextWord word = words.get(1);
        assertNotNull(word);
        assertEquals("456", word.getText());
        assertTrue(word.getConfidence() > 0.0);
        assertTrue(word.getConfidence() < 1.0);

        polygon = word.getBoundingPolygon();
        assertNotNull(polygon);
        assertEquals(4, polygon.size());
        for (ImagePoint imagePoint : polygon) {
            assertTrue(imagePoint.getX() > 0);
            assertTrue(imagePoint.getY() > 0);
        }
    }

    private static void printHttpRequestAndResponse(Response<ImageAnalysisResult> response) {
        // Print HTTP request details to console
        HttpRequest request = response.getRequest();
        LOGGER.log(LogLevel.VERBOSE, () -> " HTTP request method: " + request.getHttpMethod());
        LOGGER.log(LogLevel.VERBOSE, () -> " HTTP request URL: " + request.getUrl());
        LOGGER.log(LogLevel.VERBOSE, () -> " HTTP request headers: ");
        request.getHeaders().forEach(header -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "   " + header.getName() + ": " + header.getValue());
        });
        if (request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE).contains("application/json")) {
            LOGGER.log(LogLevel.VERBOSE, () -> " HTTP request body: " + request.getBodyAsBinaryData().toString());
        }

        // Print HTTP response details to console
        LOGGER.log(LogLevel.VERBOSE, () -> " HTTP response status code: " + response.getStatusCode());
        LOGGER.log(LogLevel.VERBOSE, () -> " HTTP response headers: ");
        response.getHeaders().forEach(header -> {
            LOGGER.log(LogLevel.VERBOSE, () -> "   " + header.getName() + ": " + header.getValue());
        });
    }

    private static void printAnalysisResults(String testName, ImageAnalysisResult result) {

        LOGGER.log(LogLevel.VERBOSE, () -> " ******************** TEST NAME: " + testName + " ******************** ");

        try {
            LOGGER.log(LogLevel.VERBOSE, () -> " Image height: " + result.getMetadata().getHeight());
            LOGGER.log(LogLevel.VERBOSE, () -> " Image width: " + result.getMetadata().getWidth());
            LOGGER.log(LogLevel.VERBOSE, () -> " Model version: " + result.getModelVersion());

            if (result.getCaption() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " Caption:");
                LOGGER.log(LogLevel.VERBOSE, () -> "   \"" + result.getCaption().getText() + "\", Confidence "
                    + String.format("%.4f", result.getCaption().getConfidence()));
            }

            if (result.getDenseCaptions() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " Dense Captions:");
                for (DenseCaption denseCaption : result.getDenseCaptions().getValues()) {
                    LOGGER.log(LogLevel.VERBOSE, () -> "   \"" + denseCaption.getText() + "\", Bounding box "
                        + denseCaption.getBoundingBox()
                        + ", Confidence " + String.format("%.4f", denseCaption.getConfidence()));
                }
            }

            if (result.getObjects() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " Objects:");
                for (DetectedObject detectedObject : result.getObjects().getValues()) {
                    LOGGER.log(LogLevel.VERBOSE, () -> "   \"" + detectedObject.getTags().get(0).getName() + "\", Bounding box "
                        + detectedObject.getBoundingBox()
                        + ", Confidence " + String.format("%.4f", detectedObject.getTags().get(0).getConfidence()));
                }
            }

            if (result.getTags() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " Tags:");
                for (DetectedTag tag : result.getTags().getValues()) {
                    LOGGER.log(LogLevel.VERBOSE, () -> "   \"" + tag.getName() + "\", Confidence "
                        + String.format("%.4f", tag.getConfidence()));
                }
            }

            if (result.getPeople() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " People:");
                for (DetectedPerson person : result.getPeople().getValues()) {
                    LOGGER.log(LogLevel.VERBOSE, () -> "   Bounding box " + person.getBoundingBox()
                        + ", Confidence " + String.format("%.4f", person.getConfidence()));
                }
            }

            if (result.getSmartCrops() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " Crop Suggestions:");
                for (CropRegion cropRegion : result.getSmartCrops().getValues()) {
                    LOGGER.log(LogLevel.VERBOSE, () -> "   Aspect ratio " + cropRegion.getAspectRatio()
                        + ": Bounding box " + cropRegion.getBoundingBox());
                }
            }

            if (result.getRead() != null) {
                LOGGER.log(LogLevel.VERBOSE, () -> " Read:");
                for (DetectedTextLine line : result.getRead().getBlocks().get(0).getLines()) {
                    LOGGER.log(LogLevel.VERBOSE, () -> "   Line: '" + line.getText()
                        + "', Bounding polygon " + line.getBoundingPolygon());
                    for (DetectedTextWord word : line.getWords()) {
                        LOGGER.log(LogLevel.VERBOSE, () -> "     Word: '" + word.getText()
                            + "', Bounding polygon " + word.getBoundingPolygon()
                            + ", Confidence " + String.format("%.4f", word.getConfidence()));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Error printing analysis results", e);
        }
    }
}
