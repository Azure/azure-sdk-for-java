// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for Map Legend operations (Test07).
 * Mirrors sync tests in TestPlanetaryComputer07MapLegendsTests.
 */
@Tag("Tiler")
@Tag("Legends")
@Tag("Async")
public class TestPlanetaryComputer07AsyncMapLegendsTests extends PlanetaryComputerTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Tag("ClassMapLegend")
    public void test07_01_GetClassMapLegendAsync() {
        String classmapName = "mtbs-severity";

        StepVerifier
            .create(
                dataAsyncClient.getClassMapLegendWithResponse(classmapName, null).map(response -> response.getValue()))
            .assertNext(responseData -> {
                try {
                    JsonNode root = objectMapper.readTree(responseData.toBytes());

                    assertTrue(root.isObject(), "Response should be a JSON object");

                    int classCount = root.size();
                    assertTrue(classCount > 0, "Response should not be empty");

                    String[] expectedClasses = { "0", "1", "2", "3", "4", "5", "6" };
                    for (String expectedClass : expectedClasses) {
                        assertTrue(root.has(expectedClass), "Class '" + expectedClass + "' should be in response");
                    }

                    Iterator<String> fieldNames = root.fieldNames();
                    while (fieldNames.hasNext()) {
                        String classValue = fieldNames.next();
                        JsonNode colorElement = root.get(classValue);

                        assertTrue(colorElement.isArray(), "Color for class '" + classValue + "' should be an array");
                        assertEquals(4, colorElement.size(),
                            "Color for class '" + classValue + "' should have 4 RGBA values");

                        for (int i = 0; i < 4; i++) {
                            JsonNode component = colorElement.get(i);
                            assertTrue(component.isNumber(), "Component should be a number");
                            int value = component.asInt();
                            assertTrue(value >= 0 && value <= 255, "Component should be 0-255");
                        }
                    }
                } catch (Exception e) {
                    fail("Failed to parse JSON response: " + e.getMessage());
                }
            })
            .verifyComplete();
    }

    @Test
    @Tag("IntervalLegend")
    public void test07_02_GetIntervalLegendAsync() {
        String colormapName = "modis-64A1";

        StepVerifier
            .create(
                dataAsyncClient.getIntervalLegendWithResponse(colormapName, null).map(response -> response.getValue()))
            .assertNext(responseData -> {
                try {
                    JsonNode root = objectMapper.readTree(responseData.toBytes());

                    assertTrue(root.isArray(), "Response should be a JSON array");
                    int itemCount = root.size();
                    assertTrue(itemCount > 0, "Response should not be empty");

                    for (JsonNode item : root) {
                        assertTrue(item.isArray(), "Each item should be an array");
                        assertTrue(item.size() >= 2, "Each item should have at least 2 elements [value, color]");
                    }
                } catch (Exception e) {
                    fail("Failed to parse JSON response: " + e.getMessage());
                }
            })
            .verifyComplete();
    }

    @Test
    @Tag("Legend")
    public void test07_03_GetLegendAsPngAsync() {
        String colorMapName = "rdylgn";

        StepVerifier.create(dataAsyncClient.getLegend(colorMapName)).assertNext(imageData -> {
            assertNotNull(imageData, "Legend image should not be null");

            byte[] imageBytes = imageData.toBytes();
            assertTrue(imageBytes.length > 100,
                String.format("Legend image should be substantial, got only %d bytes", imageBytes.length));

            // Verify PNG magic bytes
            byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
            for (int i = 0; i < pngMagic.length; i++) {
                assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
            }
        }).verifyComplete();
    }

    @Test
    @Tag("Legend")
    public void test07_04_GetLegendViridisAsync() {
        String colorMapName = "viridis";

        StepVerifier.create(dataAsyncClient.getLegend(colorMapName)).assertNext(imageData -> {
            assertNotNull(imageData, "Legend image should not be null");

            byte[] imageBytes = imageData.toBytes();
            assertTrue(imageBytes.length > 100,
                String.format("Legend image should be substantial, got only %d bytes", imageBytes.length));

            // Verify PNG magic bytes
            byte[] pngMagic = new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
            for (int i = 0; i < pngMagic.length; i++) {
                assertEquals(pngMagic[i], imageBytes[i], String.format("PNG magic byte %d mismatch", i));
            }
        }).verifyComplete();
    }
}
