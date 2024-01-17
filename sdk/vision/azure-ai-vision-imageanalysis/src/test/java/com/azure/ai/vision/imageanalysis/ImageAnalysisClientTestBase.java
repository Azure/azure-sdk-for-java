// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.imageanalysis.tests;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.net.URL;
import java.net.MalformedURLException;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.BinaryData;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;

// See https://junit.org/junit5/docs/5.0.1/api/org/junit/jupiter/api/Assertions.html
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.ai.vision.imageanalysis.*;
import com.azure.ai.vision.imageanalysis.models.*;

class ImageAnalysisClientTestBase extends TestProxyTestBase {

    final Boolean printResults = false; // Set to true to print results to console window

    // We a single image (the same one) for all error-free tests, one hosted on the web and one local
    final String imageUrl = "https://aka.ms/azai/vision/image-analysis-sample.jpg";
    final String imageFile = "./src/test/java/com/azure/ai/vision/imageanalysis/sample.jpg";

    // The client that will be used for tests
    private ImageAnalysisClient client = null;
    private ImageAnalysisAsyncClient asyncClient = null;

    // Tests that use the standard Image Analysis model must call this method first
    protected void createClientForStandardAnalysis(Boolean sync) {
        createClient("VISION_ENDPOINT", "VISION_KEY", sync, null);
    }

    //  Tests that use the standard Image Analysis model and need to add custom query parameters must call this method first
    protected void createClientForStandardAnalysis(Boolean sync, List<Entry<String, String>> queryParams) {
        createClient("VISION_ENDPOINT", "VISION_KEY", sync, queryParams);
    }

    // Tests that uses bad key to test authentication failure
    protected void createClientForAuthenticationFailure(Boolean sync) {
        createClient("VISION_ENDPOINT", "VISION_KEY_FAKE", sync, null);
    }

    private void createClient(String endpointEnvVar, String keyEnvVar, Boolean sync, List<Entry<String, String>> queryParams) {

        String endpoint = Configuration.getGlobalConfiguration().get(endpointEnvVar); // Read endpoint URL from environment variable
        if (endpoint == null) {
            endpoint = "https://fake-resource-name.cognitiveservices.azure.com";
        }

        String key = Configuration.getGlobalConfiguration().get(keyEnvVar); // Read real key from environment variable 
        if (key == null || keyEnvVar == "VISION_KEY_FAKE") {
            key = "00000000000000000000000000000000";
        }

        ImageAnalysisClientBuilder imageAnalysisClientBuilder =
                new ImageAnalysisClientBuilder()
                        .endpoint(endpoint)
                        .credential(new KeyCredential(key))
                        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Apply any optional custom query parameters
        if (queryParams != null) {
            for (Entry<String, String> queryParam : queryParams) {
                imageAnalysisClientBuilder.addPolicy(new ImageAnalysisAddQueryParamPolicy(queryParam.getKey(), queryParam.getValue()));
            }
        }

        // Handle Test Proxy recording and playback modes
        if (getTestMode() == TestMode.PLAYBACK) {
            imageAnalysisClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            imageAnalysisClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (sync) {
            client = imageAnalysisClientBuilder.buildClient();
        } else {
            asyncClient = imageAnalysisClientBuilder.buildAsyncClient();
        }
    }

    protected void doAnalysis(
        String testName,
        Boolean sync,
        String imageSource,
        List<VisualFeatures> visualFeatures,
        ImageAnalysisOptions options) throws MalformedURLException {

        Boolean fromUrl = imageSource.startsWith("http");
        URL imageUrl = null;
        Boolean genderNeutralCaption = null;
        List<Double> aspectRatios = null;

        if (options != null) {
            genderNeutralCaption = options.isGenderNeutralCaption();
            aspectRatios = options.getSmartCropsAspectRatios();
        }

        if (sync) {
            ImageAnalysisResult result = null;
            if (fromUrl) {
                result = client.analyze(
                    new URL(imageSource),
                    visualFeatures,
                    options);
            } else {
                result = client.analyze(
                    BinaryData.fromFile(new File(imageSource).toPath()),
                    visualFeatures,
                    options);
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
                result = asyncClient.analyze(
                    new URL(imageSource),
                    visualFeatures,
                    options).block();
            } else {
                result = asyncClient.analyze(
                    BinaryData.fromFile(new File(imageSource).toPath()),
                    visualFeatures,
                    options).block();
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
        String expectedMessageContains) throws MalformedURLException {

        Boolean fromUrl = imageSource.startsWith("http");
        ImageAnalysisResult result = null;
        URL imageUrl = null;

        if (sync) {
            try {
                if (fromUrl) {
                    result = client.analyze(
                        new URL(imageSource),
                        visualFeatures,
                        options);
                } else {
                    result = client.analyze(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        options);
                }
            } catch (HttpResponseException e) {
                System.out.println("Expected exception: " + e.getMessage());
                assertEquals(expectedStatusCode, e.getResponse().getStatusCode());
                assertTrue(e.getMessage().contains(expectedMessageContains));
                return;
            }
        } else {
            try {
                if (fromUrl) {
                    result = asyncClient.analyze(
                        new URL(imageSource),
                        visualFeatures,
                        options).block();
                } else {
                    result = asyncClient.analyze(
                        BinaryData.fromFile(new File(imageSource).toPath()),
                        visualFeatures,
                        options).block();
                }
            } catch (HttpResponseException e) {
                System.out.println("Expected exception: " + e.getMessage());
                assertEquals(expectedStatusCode, e.getResponse().getStatusCode());
                assertTrue(e.getMessage().contains(expectedMessageContains));
                return;
            }
        }

        System.out.println("Test should have thrown an exception, but it did not");
        assertTrue(false);
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

        Boolean found1 = false;
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
            if (tag.getName().toLowerCase().equals("person")) {
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

        Boolean found1 = false, found2 = false;
        for (DetectedTag tag : tagsResult.getValues()) {
            assertNotNull(tag.getName());
            assertFalse(tag.getName().isEmpty());
            assertTrue(tag.getConfidence() > 0.0);
            assertTrue(tag.getConfidence() < 1.0);
            // We expect to see both of these in the list of tags
            if (tag.getName().toLowerCase().equals("person")) {
                found1 = true;
            }
            if (tag.getName().toLowerCase().equals("laptop")) {
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
        assertTrue(result.getPeople().getValues().size() > 0);

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
            assertTrue(listCropRegions.size() == 1);
            assertTrue(listCropRegions.get(0).getAspectRatio() >= 0.5);
            assertTrue(listCropRegions.get(0).getAspectRatio() <= 2.0);
        } else {
            assertTrue(listCropRegions.size() == aspectRatios.size());
            for (int i = 0; i < listCropRegions.size(); i++) {
                assertTrue(listCropRegions.get(i).getAspectRatio() == aspectRatios.get(i));
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
        assertTrue(line.getText().equals("Sample text"));

        List<ImagePoint> polygon = line.getBoundingPolygon();
        assertNotNull(polygon);
        assertEquals(4, polygon.size());
        for (int i = 0; i < polygon.size(); i++) {
            assertTrue(polygon.get(i).getX() > 0);
            assertTrue(polygon.get(i).getY() > 0);
        }

        // Do some verifications on the 3rd line
        line = lines.get(2);
        assertNotNull(line);
        assertTrue(line.getText().equals("123 456"));

        List<DetectedTextWord> words = line.getWords();
        assertNotNull(words);
        assertEquals(2, words.size());

        DetectedTextWord word = words.get(1);
        assertNotNull(word);
        assertTrue(word.getText().equals("456"));
        assertTrue(word.getConfidence() > 0.0);
        assertTrue(word.getConfidence() < 1.0);

        polygon = word.getBoundingPolygon();
        assertNotNull(polygon);
        assertEquals(4, polygon.size());
        for (int i = 0; i < polygon.size(); i++) {
            assertTrue(polygon.get(i).getX() > 0);
            assertTrue(polygon.get(i).getY() > 0);
        }
    }

    private static void printAnalysisResults(String testName, ImageAnalysisResult result) {

        System.out.println(" ******************** TEST NAME: " + testName + " ******************** ");

        try {
            System.out.println(" Image height: " + result.getMetadata().getHeight());
            System.out.println(" Image width: " + result.getMetadata().getWidth());
            System.out.println(" Model version: " + result.getModelVersion());

            if (result.getCaption() != null) {
                System.out.println(" Caption:");
                System.out.println("   \"" + result.getCaption().getText() + "\", Confidence "
                    + String.format("%.4f", result.getCaption().getConfidence()));
            }

            if (result.getDenseCaptions() != null) {
                System.out.println(" Dense Captions:");
                for (DenseCaption denseCaption : result.getDenseCaptions().getValues()) {
                    System.out.println("   \"" + denseCaption.getText() + "\", Bounding box "
                        + denseCaption.getBoundingBox()
                        + ", Confidence " + String.format("%.4f", denseCaption.getConfidence()));
                }
            }

            if (result.getObjects() != null) {
                System.out.println(" Objects:");
                for (DetectedObject detectedObject : result.getObjects().getValues()) {
                    System.out.println("   \"" + detectedObject.getTags().get(0).getName() + "\", Bounding box "
                        + detectedObject.getBoundingBox()
                        + ", Confidence " + String.format("%.4f", detectedObject.getTags().get(0).getConfidence()));
                }
            }

            if (result.getTags() != null) {
                System.out.println(" Tags:");
                for (DetectedTag tag : result.getTags().getValues()) {
                    System.out.println("   \"" + tag.getName() + "\", Confidence "
                        + String.format("%.4f", tag.getConfidence()));
                }
            }

            if (result.getPeople() != null) {
                System.out.println(" People:");
                for (DetectedPerson person : result.getPeople().getValues()) {
                    System.out.println("   Bounding box " + person.getBoundingBox()
                        + ", Confidence " + String.format("%.4f", person.getConfidence()));
                }
            }

            if (result.getSmartCrops() != null) {
                System.out.println(" Crop Suggestions:");
                for (CropRegion cropRegion : result.getSmartCrops().getValues()) {
                    System.out.println("   Aspect ratio " + cropRegion.getAspectRatio()
                        + ": Bounding box " + cropRegion.getBoundingBox());
                }
            }

            if (result.getRead() != null) {
                System.out.println(" Read:");
                for (DetectedTextLine line : result.getRead().getBlocks().get(0).getLines()) {
                    System.out.println("   Line: '" + line.getText()
                        + "', Bounding polygon " + line.getBoundingPolygon());
                    for (DetectedTextWord word : line.getWords()) {
                        System.out.println("     Word: '" + word.getText()
                            + "', Bounding polygon " + word.getBoundingPolygon()
                            + ", Confidence " + String.format("%.4f", word.getConfidence()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
